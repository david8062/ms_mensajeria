package com.IusCloud.messaging.core.features.notifications.application.usecase;

import com.IusCloud.messaging.core.features.notifications.application.dto.NotificationResponseDTO;
import com.IusCloud.messaging.core.features.notifications.application.mapper.NotificationMapper;
import com.IusCloud.messaging.core.features.notifications.domain.port.in.GetNotificationByIdPort;
import com.IusCloud.messaging.core.features.notifications.domain.port.out.NotificationRepository;
import com.IusCloud.messaging.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetNotificationByIdUseCase implements GetNotificationByIdPort {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDTO execute(UUID id, UUID tenantId) {
        var entity = repository.findById(id)
                .filter(n -> n.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        return mapper.toResponse(entity);
    }
}
