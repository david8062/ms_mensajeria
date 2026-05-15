package com.IusCloud.messaging.core.features.notifications.domain.port.out;

import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import com.IusCloud.messaging.shared.enums.NotificationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    NotificationEntity save(NotificationEntity entity);

    Optional<NotificationEntity> findById(UUID id);

    Optional<NotificationEntity> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);

    Optional<NotificationEntity> findByEvolutionMessageId(String evolutionMessageId);

    List<NotificationEntity> findDueScheduled(Instant now, int limit);

    List<NotificationEntity> findByTenantIdAndStatus(UUID tenantId, NotificationStatus status);
}
