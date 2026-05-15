package com.IusCloud.messaging.core.features.notifications.domain.port.in;

import com.IusCloud.messaging.core.features.notifications.application.dto.NotificationResponseDTO;

import java.util.UUID;

public interface GetNotificationByIdPort {
    NotificationResponseDTO execute(UUID id, UUID tenantId);
}
