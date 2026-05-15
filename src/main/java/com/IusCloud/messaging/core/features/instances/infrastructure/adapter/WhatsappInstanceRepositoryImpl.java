package com.IusCloud.messaging.core.features.instances.infrastructure.adapter;

import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.core.features.instances.infrastructure.persistence.WhatsappInstanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WhatsappInstanceRepositoryImpl implements WhatsappInstanceRepository {

    private final WhatsappInstanceJpaRepository jpa;

    @Override
    public WhatsappInstanceEntity save(WhatsappInstanceEntity entity) {
        return jpa.save(entity);
    }

    @Override
    public Optional<WhatsappInstanceEntity> findByTenantId(UUID tenantId) {
        return jpa.findByTenantIdAndDeletedAtIsNull(tenantId);
    }

    @Override
    public Optional<WhatsappInstanceEntity> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<WhatsappInstanceEntity> findByInstanceName(String instanceName) {
        return jpa.findByInstanceNameAndDeletedAtIsNull(instanceName);
    }
}
