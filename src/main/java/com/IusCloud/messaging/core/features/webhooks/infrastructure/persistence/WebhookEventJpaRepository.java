package com.IusCloud.messaging.core.features.webhooks.infrastructure.persistence;

import com.IusCloud.messaging.core.base.BaseRepository;
import com.IusCloud.messaging.core.features.webhooks.domain.model.WebhookEventEntity;

import java.util.UUID;

public interface WebhookEventJpaRepository extends BaseRepository<WebhookEventEntity, UUID> {
}
