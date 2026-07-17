package com.IusCloud.messaging.core.features.assistant;

import com.IusCloud.messaging.core.common.evolution.EvolutionApiClient;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Atiende, de forma asíncrona, los mensajes que llegan a la LÍNEA DE PLATAFORMA (IusCloud): le
 * pide la respuesta al asistente de ms-ia y la envía por WhatsApp. Se hace en un hilo aparte para
 * no bloquear el ack del webhook de Evolution (la respuesta del asistente implica una llamada a
 * Claude, que puede tardar segundos).
 */
@Component
@Slf4j
public class WhatsappAssistantDispatcher {

    private final AssistantClient assistantClient;
    private final EvolutionApiClient evolutionApiClient;
    private final String platformInstance;

    // Pool pequeño y daemon: el volumen es bajo (ritmo lento del derecho) y no debe frenar el apagado.
    private final ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "whatsapp-assistant");
        t.setDaemon(true);
        return t;
    });

    public WhatsappAssistantDispatcher(
            AssistantClient assistantClient,
            EvolutionApiClient evolutionApiClient,
            @Value("${messaging.platform.instance-name:IusCloud}") String platformInstance) {
        this.assistantClient = assistantClient;
        this.evolutionApiClient = evolutionApiClient;
        this.platformInstance = platformInstance;
    }

    /**
     * Si el mensaje entró por la línea de plataforma, dispara la respuesta del asistente en
     * segundo plano. Los mensajes de las instancias de los abogados (abogado→cliente) se ignoran.
     */
    public void maybeReply(String instanceName, String senderPhone, String text) {
        if (instanceName == null || !instanceName.equals(platformInstance)) {
            return;
        }
        if (senderPhone == null || senderPhone.isBlank() || text == null || text.isBlank()) {
            return;
        }
        pool.submit(() -> {
            try {
                AssistantClient.AssistantReply reply = assistantClient.requestReply(senderPhone, text);
                if (reply == null) {
                    return;
                }
                if (reply.reply() != null && !reply.reply().isBlank()) {
                    evolutionApiClient.sendText(platformInstance, senderPhone, reply.reply());
                }
                if (reply.media() != null) {
                    for (AssistantClient.Media m : reply.media()) {
                        try {
                            evolutionApiClient.sendMedia(platformInstance, senderPhone, m.url(), m.filename());
                        } catch (Exception e) {
                            log.warn("No se pudo enviar el archivo {} a {}: {}", m.filename(), senderPhone, e.getMessage());
                        }
                    }
                }
                // Aviso de lead al dueño (número distinto al remitente).
                AssistantClient.LeadNotify lead = reply.leadNotify();
                if (lead != null && lead.phone() != null && !lead.phone().isBlank()
                        && lead.text() != null && !lead.text().isBlank()) {
                    try {
                        evolutionApiClient.sendText(platformInstance, lead.phone(), lead.text());
                    } catch (Exception e) {
                        log.warn("No se pudo avisar del lead a {}: {}", lead.phone(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo atender el mensaje del asistente de {}: {}", senderPhone, e.getMessage());
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        pool.shutdown();
    }
}
