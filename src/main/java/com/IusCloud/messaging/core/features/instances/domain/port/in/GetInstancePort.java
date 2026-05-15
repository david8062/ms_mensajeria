package com.IusCloud.messaging.core.features.instances.domain.port.in;

import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceResponseDTO;

import java.util.UUID;

public interface GetInstancePort {
    WhatsappInstanceResponseDTO execute(UUID tenantId);
}
