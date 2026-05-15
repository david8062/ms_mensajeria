package com.IusCloud.messaging.core.features.instances.application.dto;

import com.IusCloud.messaging.core.base.BaseDTO;
import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class WhatsappInstanceResponseDTO extends BaseDTO {
    private UUID tenantId;
    private String instanceName;
    private String phoneNumber;
    private WhatsappInstanceStatus status;
    private Instant lastConnectedAt;
}
