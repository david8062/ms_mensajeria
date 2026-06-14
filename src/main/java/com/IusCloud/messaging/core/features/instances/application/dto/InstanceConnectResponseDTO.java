package com.IusCloud.messaging.core.features.instances.application.dto;

import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InstanceConnectResponseDTO {
    private String instanceName;
    private WhatsappInstanceStatus status;
    private String base64Qr;
    private String pairingCode;
}
