package com.IusCloud.messaging.core.features.notifications.application.dto;

import com.IusCloud.messaging.shared.templates.NotificationTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CreateNotificationRequestDTO(

        @NotNull(message = "templateCode es obligatorio")
        NotificationTemplate templateCode,

        @NotBlank(message = "recipientPhone es obligatorio")
        String recipientPhone,

        UUID clientId,

        Map<String, Object> variables,

        Instant scheduledAt,

        String idempotencyKey,

        Map<String, Object> payload
) {
}
