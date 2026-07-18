package com.IusCloud.messaging.core.common.evolution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Estrangula los envíos salientes por WhatsApp para no disparar la detección de spam de Meta
 * (que restringe la cuenta ~6h y escala a ban permanente del número).
 *
 * <p>Espacia los mensajes <b>por instancia</b> —cada número tiene su propio presupuesto de tasa—
 * con un intervalo mínimo configurable + un jitter aleatorio, de modo que nunca salgan en ráfaga
 * (el patrón que Meta castiga). Es un cuello de botella <i>deliberado</i>: hace backpressure
 * bloqueando el hilo que envía hasta que sea seguro, que es justo lo que queremos.
 *
 * <p>Volúmenes actuales (un despacho, pocas notificaciones) hacen que el bloqueo sea marginal en
 * el chat 1:1 del asistente (la respuesta de Claude ya tarda) y solo espacie los envíos masivos
 * (alertas de la Rama, OTP), que es donde está el riesgo.
 */
@Component
@Slf4j
public class WhatsappSendThrottle {

    private static final String DEFAULT_KEY = "__default__";

    private final long minGapMs;
    private final long jitterMs;

    /** Un candado por instancia para serializar sus envíos sin bloquear a las demás. */
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastSentAt = new ConcurrentHashMap<>();

    public WhatsappSendThrottle(
            @Value("${whatsapp.throttle.min-gap-ms:4000}") long minGapMs,
            @Value("${whatsapp.throttle.jitter-ms:4000}") long jitterMs) {
        this.minGapMs = Math.max(0, minGapMs);
        this.jitterMs = Math.max(0, jitterMs);
    }

    /**
     * Bloquea hasta que sea seguro enviar por {@code instanceName}: garantiza al menos
     * {@code minGapMs} (+ jitter) desde el último envío de esa misma instancia.
     */
    public void pace(String instanceName) {
        String key = instanceName == null ? DEFAULT_KEY : instanceName;
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            Long last = lastSentAt.get(key);
            if (last != null) {
                long target = minGapMs + (jitterMs > 0
                        ? ThreadLocalRandom.current().nextLong(jitterMs + 1)
                        : 0);
                long waitMs = last + target - System.currentTimeMillis();
                if (waitMs > 0) {
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            lastSentAt.put(key, System.currentTimeMillis());
        }
    }
}
