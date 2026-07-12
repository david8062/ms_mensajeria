package com.IusCloud.messaging.core.features.notifications.infrastructure.inbound;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Verificación de un prospecto: todavía NO existe tenant, así que no hay tenantId. */
@Getter
@Setter
public class SendVerificationRequestDTO {

    @NotBlank
    private String phone;

    @NotBlank
    private String otp;
}
