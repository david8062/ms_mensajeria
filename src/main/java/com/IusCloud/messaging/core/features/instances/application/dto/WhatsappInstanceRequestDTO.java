package com.IusCloud.messaging.core.features.instances.application.dto;

import jakarta.validation.constraints.NotBlank;

public record WhatsappInstanceRequestDTO(
        @NotBlank(message = "instanceName es obligatorio")
        String instanceName,
        String phoneNumber,
        String webhookSecret
) {
}
