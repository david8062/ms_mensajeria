package com.IusCloud.messaging.core.features.webhooks.domain.port.out;

import com.IusCloud.messaging.core.features.webhooks.domain.model.WhatsappInboundMessageEntity;

import java.util.Optional;

public interface WhatsappInboundMessageRepository {
    WhatsappInboundMessageEntity save(WhatsappInboundMessageEntity entity);
    Optional<WhatsappInboundMessageEntity> findByEvolutionMessageId(String evolutionMessageId);
}
