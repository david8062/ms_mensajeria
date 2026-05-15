package com.IusCloud.messaging.core.features.webhooks.domain.port.out;

import com.IusCloud.messaging.core.features.webhooks.domain.model.WebhookEventEntity;

public interface WebhookEventRepository {
    WebhookEventEntity save(WebhookEventEntity entity);
}
