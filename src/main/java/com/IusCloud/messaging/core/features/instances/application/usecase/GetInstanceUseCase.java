package com.IusCloud.messaging.core.features.instances.application.usecase;

import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceResponseDTO;
import com.IusCloud.messaging.core.features.instances.application.mapper.WhatsappInstanceMapper;
import com.IusCloud.messaging.core.features.instances.domain.port.in.GetInstancePort;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetInstanceUseCase implements GetInstancePort {

    private final WhatsappInstanceRepository repository;
    private final WhatsappInstanceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public WhatsappInstanceResponseDTO execute(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No hay instancia configurada para este tenant"));
    }
}
