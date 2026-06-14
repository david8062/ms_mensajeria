package com.IusCloud.messaging.core.features.notifications.infrastructure.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SendOtpRequestDTO {

    @NotNull
    private UUID tenantId;

    @NotBlank
    private String phone;

    @NotBlank
    private String otp;
}
