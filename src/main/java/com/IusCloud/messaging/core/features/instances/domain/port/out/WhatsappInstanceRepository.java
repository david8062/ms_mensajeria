package com.IusCloud.messaging.core.features.instances.domain.port.out;

import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;

import java.util.Optional;
import java.util.UUID;

public interface WhatsappInstanceRepository {

    WhatsappInstanceEntity save(WhatsappInstanceEntity entity);

    Optional<WhatsappInstanceEntity> findByTenantId(UUID tenantId);

    Optional<WhatsappInstanceEntity> findById(UUID id);

    Optional<WhatsappInstanceEntity> findByInstanceName(String instanceName);
}
