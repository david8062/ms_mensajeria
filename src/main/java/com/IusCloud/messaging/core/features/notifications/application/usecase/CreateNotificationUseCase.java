package com.IusCloud.messaging.core.features.notifications.application.usecase;

import com.IusCloud.messaging.core.features.notifications.application.dto.CreateNotificationRequestDTO;
import com.IusCloud.messaging.core.features.notifications.application.dto.NotificationResponseDTO;
import com.IusCloud.messaging.core.features.notifications.application.mapper.NotificationMapper;
import com.IusCloud.messaging.core.features.notifications.domain.port.in.CreateNotificationPort;
import com.IusCloud.messaging.core.features.notifications.domain.port.in.DispatchNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateNotificationUseCase implements CreateNotificationPort {

    private final NotificationPersistenceService persistence;
    private final NotificationMapper mapper;
    private final DispatchNotificationPort dispatcher;

    @Override
    public NotificationResponseDTO execute(CreateNotificationRequestDTO request, UUID tenantId) {
        NotificationPersistenceService.PersistResult result = persistence.persistPending(request, tenantId);

        if (result.shouldSendNow()) {
            dispatcher.dispatch(result.entity());
        }

        return mapper.toResponse(result.entity());
    }
}
