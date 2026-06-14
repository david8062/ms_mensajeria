package com.IusCloud.messaging.core.features.instances.application.usecase;

import com.IusCloud.messaging.core.common.evolution.EvolutionApiClient;
import com.IusCloud.messaging.core.features.instances.application.dto.InstanceConnectResponseDTO;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.in.ConnectInstancePort;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import com.IusCloud.messaging.shared.exceptions.BusinessException;
import com.IusCloud.messaging.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectInstanceUseCase implements ConnectInstancePort {

    private final WhatsappInstanceRepository repository;
    private final EvolutionApiClient evolutionApiClient;

    @Override
    @Transactional
    public InstanceConnectResponseDTO execute(UUID tenantId) {
        WhatsappInstanceEntity instance = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay instancia configurada. Usa PUT /api/v1/instances primero."));

        // Verificar estado real en Evolution API (la DB puede estar desincronizada)
        String evolutionState = evolutionApiClient.getConnectionState(instance.getInstanceName());
        if ("open".equalsIgnoreCase(evolutionState)) {
            if (!WhatsappInstanceStatus.CONNECTED.equals(instance.getStatus())) {
                instance.setStatus(WhatsappInstanceStatus.CONNECTED);
                repository.save(instance);
            }
            throw new BusinessException("ALREADY_CONNECTED",
                    "La instancia ya está conectada en Evolution API. Desconéctala primero si deseas re-vincular.");
        }

        if (WhatsappInstanceStatus.CONNECTED.equals(instance.getStatus())) {
            throw new BusinessException("ALREADY_CONNECTED",
                    "La instancia ya está conectada. Desconéctala primero si deseas re-vincular.");
        }

        instance.setStatus(WhatsappInstanceStatus.CONNECTING);
        repository.save(instance);

        EvolutionApiClient.InstanceConnectResult result =
                evolutionApiClient.connectInstance(instance.getInstanceName());

        WhatsappInstanceStatus nextStatus = result.base64Qr() != null
                ? WhatsappInstanceStatus.QR_PENDING
                : WhatsappInstanceStatus.CONNECTING;

        instance.setStatus(nextStatus);
        repository.save(instance);

        log.info("Conexión iniciada para tenant={} instance={} status={}", tenantId, instance.getInstanceName(), nextStatus);

        return new InstanceConnectResponseDTO(
                instance.getInstanceName(),
                nextStatus,
                result.base64Qr(),
                result.pairingCode()
        );
    }
}
