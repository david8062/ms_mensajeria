package com.IusCloud.messaging.core.features.webhooks.infrastructure.adapter;

import com.IusCloud.messaging.core.features.webhooks.domain.model.WebhookEventEntity;
import com.IusCloud.messaging.core.features.webhooks.domain.port.out.WebhookEventRepository;
import com.IusCloud.messaging.core.features.webhooks.infrastructure.persistence.WebhookEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookEventRepositoryImpl implements WebhookEventRepository {

    private final WebhookEventJpaRepository jpa;

    @Override
    public WebhookEventEntity save(WebhookEventEntity entity) {
        return jpa.save(entity);
    }
}
