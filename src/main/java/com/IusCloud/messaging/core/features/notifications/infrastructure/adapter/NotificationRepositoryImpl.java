package com.IusCloud.messaging.core.features.notifications.infrastructure.adapter;

import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import com.IusCloud.messaging.core.features.notifications.domain.port.out.NotificationRepository;
import com.IusCloud.messaging.core.features.notifications.infrastructure.persistence.NotificationJpaRepository;
import com.IusCloud.messaging.shared.enums.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpa;

    @Override
    public NotificationEntity save(NotificationEntity entity) {
        return jpa.save(entity);
    }

    @Override
    public Optional<NotificationEntity> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<NotificationEntity> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey) {
        return jpa.findByTenantIdAndIdempotencyKeyAndDeletedAtIsNull(tenantId, idempotencyKey);
    }

    @Override
    public Optional<NotificationEntity> findByEvolutionMessageId(String evolutionMessageId) {
        return jpa.findByEvolutionMessageIdAndDeletedAtIsNull(evolutionMessageId);
    }

    @Override
    public List<NotificationEntity> findDueScheduled(Instant now, int limit) {
        return jpa.findDueScheduled(now, PageRequest.of(0, limit));
    }

    @Override
    public List<NotificationEntity> findByTenantIdAndStatus(UUID tenantId, NotificationStatus status) {
        return jpa.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status);
    }
}
