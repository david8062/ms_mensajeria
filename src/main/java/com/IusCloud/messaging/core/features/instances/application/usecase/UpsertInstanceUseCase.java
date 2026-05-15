package com.IusCloud.messaging.core.features.instances.application.usecase;

import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceRequestDTO;
import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceResponseDTO;
import com.IusCloud.messaging.core.features.instances.application.mapper.WhatsappInstanceMapper;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.in.UpsertInstancePort;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpsertInstanceUseCase implements UpsertInstancePort {

    private final WhatsappInstanceRepository repository;
    private final WhatsappInstanceMapper mapper;

    @Override
    @Transactional
    public WhatsappInstanceResponseDTO execute(WhatsappInstanceRequestDTO request, UUID tenantId) {
        WhatsappInstanceEntity entity = repository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    WhatsappInstanceEntity created = new WhatsappInstanceEntity();
                    created.setTenantId(tenantId);
                    created.setStatus(WhatsappInstanceStatus.DISCONNECTED);
                    return created;
                });

        entity.setInstanceName(request.instanceName());
        entity.setPhoneNumber(request.phoneNumber());
        if (request.webhookSecret() != null) {
            entity.setWebhookSecret(request.webhookSecret());
        }

        return mapper.toResponse(repository.save(entity));
    }
}
