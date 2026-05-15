package com.IusCloud.messaging.core.features.webhooks.infrastructure.persistence;

import com.IusCloud.messaging.core.base.BaseRepository;
import com.IusCloud.messaging.core.features.webhooks.domain.model.WhatsappInboundMessageEntity;

import java.util.Optional;
import java.util.UUID;

public interface WhatsappInboundMessageJpaRepository extends BaseRepository<WhatsappInboundMessageEntity, UUID> {
    Optional<WhatsappInboundMessageEntity> findByEvolutionMessageIdAndDeletedAtIsNull(String evolutionMessageId);
}
