package com.IusCloud.messaging.core.features.instances.domain.port.in;

import com.IusCloud.messaging.core.features.instances.application.dto.InstanceConnectResponseDTO;

import java.util.UUID;

public interface GetInstanceQrPort {
    InstanceConnectResponseDTO execute(UUID tenantId);
}
