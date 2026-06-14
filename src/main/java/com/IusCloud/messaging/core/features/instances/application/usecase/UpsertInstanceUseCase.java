package com.IusCloud.messaging.core.features.instances.application.usecase;

import com.IusCloud.messaging.core.common.evolution.EvolutionApiClient;
import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceRequestDTO;
import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceResponseDTO;
import com.IusCloud.messaging.core.features.instances.application.mapper.WhatsappInstanceMapper;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.in.UpsertInstancePort;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import com.IusCloud.messaging.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpsertInstanceUseCase implements UpsertInstancePort {

    private final WhatsappInstanceRepository repository;
    private final WhatsappInstanceMapper mapper;
    private final EvolutionApiClient evolutionApiClient;

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

        boolean isNew = entity.getId() == null;

        // Verificar que el instanceName no esté siendo usado por otro tenant
        repository.findByInstanceName(request.instanceName())
                .filter(existing -> !existing.getTenantId().equals(tenantId))
                .ifPresent(existing -> {
                    throw new BusinessException("INSTANCE_NAME_TAKEN",
                            "El nombre de instancia '" + request.instanceName() + "' ya está en uso por otro tenant.");
                });

        entity.setInstanceName(request.instanceName());
        entity.setPhoneNumber(request.phoneNumber());
        if (request.webhookSecret() != null) {
            entity.setWebhookSecret(request.webhookSecret());
        }

        WhatsappInstanceResponseDTO saved = mapper.toResponse(repository.save(entity));

        if (isNew) {
            evolutionApiClient.ensureInstance(request.instanceName());
            log.info("Instancia registrada en Evolution API para tenant={} instance={}", tenantId, request.instanceName());
        }

        return saved;
    }
}
