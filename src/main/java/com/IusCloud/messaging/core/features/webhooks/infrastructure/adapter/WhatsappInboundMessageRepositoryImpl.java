package com.IusCloud.messaging.core.features.webhooks.infrastructure.adapter;

import com.IusCloud.messaging.core.features.webhooks.domain.model.WhatsappInboundMessageEntity;
import com.IusCloud.messaging.core.features.webhooks.domain.port.out.WhatsappInboundMessageRepository;
import com.IusCloud.messaging.core.features.webhooks.infrastructure.persistence.WhatsappInboundMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WhatsappInboundMessageRepositoryImpl implements WhatsappInboundMessageRepository {

    private final WhatsappInboundMessageJpaRepository jpa;

    @Override
    public WhatsappInboundMessageEntity save(WhatsappInboundMessageEntity entity) {
        return jpa.save(entity);
    }

    @Override
    public Optional<WhatsappInboundMessageEntity> findByEvolutionMessageId(String evolutionMessageId) {
        return jpa.findByEvolutionMessageIdAndDeletedAtIsNull(evolutionMessageId);
    }
}
