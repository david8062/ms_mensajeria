package com.IusCloud.messaging.core.features.webhooks.application.usecase;

import com.IusCloud.messaging.core.features.assistant.WhatsappAssistantDispatcher;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import com.IusCloud.messaging.core.features.notifications.domain.port.out.NotificationRepository;
import com.IusCloud.messaging.core.features.webhooks.domain.model.WebhookEventEntity;
import com.IusCloud.messaging.core.features.webhooks.domain.model.WhatsappInboundMessageEntity;
import com.IusCloud.messaging.core.features.webhooks.domain.port.in.ProcessEvolutionWebhookPort;
import com.IusCloud.messaging.core.features.webhooks.domain.port.out.WebhookEventRepository;
import com.IusCloud.messaging.core.features.webhooks.domain.port.out.WhatsappInboundMessageRepository;
import com.IusCloud.messaging.shared.enums.NotificationStatus;
import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessEvolutionWebhookUseCase implements ProcessEvolutionWebhookPort {

    private final WebhookEventRepository webhookEventRepository;
    private final WhatsappInboundMessageRepository inboundMessageRepository;
    private final NotificationRepository notificationRepository;
    private final WhatsappInstanceRepository instanceRepository;
    private final WhatsappAssistantDispatcher assistantDispatcher;

    @Override
    @Transactional
    public void execute(Map<String, Object> payload) {
        String eventType = stringOf(payload.get("event"));
        String instanceName = stringOf(payload.get("instance"));

        WhatsappInstanceEntity instance = instanceName != null
                ? instanceRepository.findByInstanceName(instanceName).orElse(null)
                : null;

        WebhookEventEntity event = new WebhookEventEntity();
        event.setInstanceId(instance != null ? instance.getId() : null);
        event.setEventType(eventType != null ? eventType : "UNKNOWN");
        event.setPayload(payload);
        event.setReceivedAt(Instant.now());

        try {
            dispatch(eventType, payload, instance);
            event.setProcessedAt(Instant.now());
        } catch (Exception ex) {
            log.error("Failed to process Evolution webhook event={} instance={}: {}",
                    eventType, instanceName, ex.getMessage(), ex);
            event.setError(ex.getMessage());
        }

        webhookEventRepository.save(event);
    }

    private void dispatch(String eventType, Map<String, Object> payload, WhatsappInstanceEntity instance) {
        if (eventType == null) return;

        switch (eventType) {
            case "messages.upsert" -> handleIncomingMessage(payload, instance);
            case "messages.update" -> handleMessageStatusUpdate(payload);
            case "connection.update" -> handleConnectionUpdate(payload, instance);
            default -> log.debug("Webhook event {} not handled explicitly — only logged", eventType);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleIncomingMessage(Map<String, Object> payload, WhatsappInstanceEntity instance) {
        if (instance == null) {
            log.warn("Inbound message without matching instance — skipping persist");
            return;
        }

        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?>)) return;
        Map<String, Object> data = (Map<String, Object>) dataObj;

        Map<String, Object> key = (Map<String, Object>) data.get("key");
        boolean fromMe = key != null && Boolean.TRUE.equals(key.get("fromMe"));
        if (fromMe) return; // mensajes salientes ya están en notifications

        String evolutionMessageId = key != null ? stringOf(key.get("id")) : null;
        if (evolutionMessageId != null
                && inboundMessageRepository.findByEvolutionMessageId(evolutionMessageId).isPresent()) {
            return; // idempotencia
        }

        String remoteJid = key != null ? stringOf(key.get("remoteJid")) : null;
        String senderPhone = extractPhoneFromJid(remoteJid);
        String content = extractTextContent(data);

        WhatsappInboundMessageEntity msg = new WhatsappInboundMessageEntity();
        msg.setTenantId(instance.getTenantId());
        msg.setInstanceId(instance.getId());
        msg.setSenderPhone(senderPhone != null ? senderPhone : "");
        msg.setContent(content);
        msg.setEvolutionMessageId(evolutionMessageId);
        msg.setReceivedAt(Instant.now());
        msg.setRawPayload(payload);

        inboundMessageRepository.save(msg);

        // Si entró por la línea de plataforma (IusCloud), el asistente responde (en segundo plano,
        // para no bloquear el ack del webhook). Los mensajes de instancias de tenants se ignoran.
        assistantDispatcher.maybeReply(instance.getInstanceName(), senderPhone, content);
    }

    @SuppressWarnings("unchecked")
    private void handleMessageStatusUpdate(Map<String, Object> payload) {
        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?>)) return;
        Map<String, Object> data = (Map<String, Object>) dataObj;

        Map<String, Object> key = (Map<String, Object>) data.get("key");
        String evolutionMessageId = key != null ? stringOf(key.get("id")) : null;
        if (evolutionMessageId == null) return;

        NotificationEntity notification = notificationRepository
                .findByEvolutionMessageId(evolutionMessageId)
                .orElse(null);
        if (notification == null) return;

        String status = stringOf(data.get("status"));
        if (status == null) return;

        switch (status.toUpperCase()) {
            case "DELIVERY_ACK", "DELIVERED" -> {
                notification.setStatus(NotificationStatus.DELIVERED);
                notification.setDeliveredAt(Instant.now());
            }
            case "READ" -> {
                notification.setStatus(NotificationStatus.READ);
                notification.setReadAt(Instant.now());
            }
            case "ERROR", "FAILED" -> {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setError("Evolution status: " + status);
            }
            default -> { /* SENT u otros — ignorar */ }
        }
        notificationRepository.save(notification);
    }

    @SuppressWarnings("unchecked")
    private void handleConnectionUpdate(Map<String, Object> payload, WhatsappInstanceEntity instance) {
        if (instance == null) {
            log.warn("connection.update sin instancia registrada — ignorado");
            return;
        }

        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?>)) return;
        Map<String, Object> data = (Map<String, Object>) dataObj;

        String state = stringOf(data.get("state"));
        if (state == null) return;

        WhatsappInstanceStatus newStatus = switch (state.toLowerCase()) {
            case "open" -> WhatsappInstanceStatus.CONNECTED;
            case "close" -> WhatsappInstanceStatus.DISCONNECTED;
            case "connecting" -> WhatsappInstanceStatus.CONNECTING;
            default -> null;
        };

        if (newStatus == null) {
            log.debug("connection.update state={} no mapeado — ignorado", state);
            return;
        }

        instance.setStatus(newStatus);
        if (WhatsappInstanceStatus.CONNECTED.equals(newStatus)) {
            instance.setLastConnectedAt(Instant.now());
        }
        instanceRepository.save(instance);
        log.info("Instancia {} cambió a estado {} vía webhook", instance.getInstanceName(), newStatus);
    }

    private static String stringOf(Object o) {
        return o != null ? o.toString() : null;
    }

    private static String extractPhoneFromJid(String jid) {
        if (jid == null) return null;
        int at = jid.indexOf('@');
        return at > 0 ? jid.substring(0, at) : jid;
    }

    @SuppressWarnings("unchecked")
    private static String extractTextContent(Map<String, Object> data) {
        Object messageObj = data.get("message");
        if (!(messageObj instanceof Map<?, ?>)) return null;
        Map<String, Object> message = (Map<String, Object>) messageObj;

        Object conv = message.get("conversation");
        if (conv != null) return conv.toString();

        Object ext = message.get("extendedTextMessage");
        if (ext instanceof Map<?, ?>) {
            Object text = ((Map<String, Object>) ext).get("text");
            if (text != null) return text.toString();
        }
        return null;
    }
}
