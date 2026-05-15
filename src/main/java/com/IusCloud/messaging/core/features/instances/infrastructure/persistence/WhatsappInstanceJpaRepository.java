package com.IusCloud.messaging.core.features.instances.infrastructure.persistence;

import com.IusCloud.messaging.core.base.BaseRepository;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;

import java.util.Optional;
import java.util.UUID;

public interface WhatsappInstanceJpaRepository extends BaseRepository<WhatsappInstanceEntity, UUID> {

    Optional<WhatsappInstanceEntity> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<WhatsappInstanceEntity> findByInstanceNameAndDeletedAtIsNull(String instanceName);
}
