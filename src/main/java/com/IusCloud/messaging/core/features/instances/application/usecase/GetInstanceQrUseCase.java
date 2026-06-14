package com.IusCloud.messaging.core.features.instances.application.usecase;

import com.IusCloud.messaging.core.common.evolution.EvolutionApiClient;
import com.IusCloud.messaging.core.features.instances.application.dto.InstanceConnectResponseDTO;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.in.GetInstanceQrPort;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import com.IusCloud.messaging.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetInstanceQrUseCase implements GetInstanceQrPort {

    private final WhatsappInstanceRepository repository;
    private final EvolutionApiClient evolutionApiClient;

    @Override
    @Transactional
    public InstanceConnectResponseDTO execute(UUID tenantId) {
        WhatsappInstanceEntity instance = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay instancia configurada para este tenant"));

        EvolutionApiClient.InstanceConnectResult result = evolutionApiClient.fetchQr(instance.getInstanceName());

        // Si Evolution no tiene QR, comprobar si ya está conectada
        if (result.base64Qr() == null) {
            String evolutionState = evolutionApiClient.getConnectionState(instance.getInstanceName());
            if ("open".equalsIgnoreCase(evolutionState)) {
                instance.setStatus(WhatsappInstanceStatus.CONNECTED);
                repository.save(instance);
                return new InstanceConnectResponseDTO(
                        instance.getInstanceName(),
                        WhatsappInstanceStatus.CONNECTED,
                        null,
                        null
                );
            }
        }

        return new InstanceConnectResponseDTO(
                instance.getInstanceName(),
                instance.getStatus(),
                result.base64Qr(),
                result.pairingCode()
        );
    }
}
