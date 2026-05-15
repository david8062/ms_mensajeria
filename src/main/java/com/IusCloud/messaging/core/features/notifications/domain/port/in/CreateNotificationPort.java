package com.IusCloud.messaging.core.features.notifications.domain.port.in;

import com.IusCloud.messaging.core.features.notifications.application.dto.CreateNotificationRequestDTO;
import com.IusCloud.messaging.core.features.notifications.application.dto.NotificationResponseDTO;

import java.util.UUID;

public interface CreateNotificationPort {
    NotificationResponseDTO execute(CreateNotificationRequestDTO request, UUID tenantId);
}
