package com.IusCloud.messaging.core.features.notifications.infrastructure.persistence;

import com.IusCloud.messaging.core.base.BaseRepository;
import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import com.IusCloud.messaging.shared.enums.NotificationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends BaseRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findByTenantIdAndIdempotencyKeyAndDeletedAtIsNull(UUID tenantId, String idempotencyKey);

    Optional<NotificationEntity> findByEvolutionMessageIdAndDeletedAtIsNull(String evolutionMessageId);

    List<NotificationEntity> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, NotificationStatus status);

    @Query("""
            SELECT n FROM NotificationEntity n
            WHERE n.status = com.IusCloud.messaging.shared.enums.NotificationStatus.SCHEDULED
              AND n.scheduledAt <= :now
              AND n.deletedAt IS NULL
            ORDER BY n.scheduledAt ASC
            """)
    List<NotificationEntity> findDueScheduled(@Param("now") Instant now, Pageable pageable);
}
