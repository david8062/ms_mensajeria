package com.IusCloud.messaging.core.features.instances.infrastructure.inbound;

import com.IusCloud.messaging.core.features.instances.application.dto.InstanceConnectResponseDTO;
import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceRequestDTO;
import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceResponseDTO;
import com.IusCloud.messaging.core.features.instances.domain.port.in.ConnectInstancePort;
import com.IusCloud.messaging.core.features.instances.domain.port.in.GetInstancePort;
import com.IusCloud.messaging.core.features.instances.domain.port.in.GetInstanceQrPort;
import com.IusCloud.messaging.core.features.instances.domain.port.in.UpsertInstancePort;
import com.IusCloud.messaging.shared.responses.ApiResponse;
import com.IusCloud.messaging.shared.responses.ResponseUtil;
import com.IusCloud.messaging.shared.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instances")
@RequiredArgsConstructor
public class WhatsappInstanceController {

    private final UpsertInstancePort upsertInstanceUseCase;
    private final GetInstancePort getInstanceUseCase;
    private final ConnectInstancePort connectInstanceUseCase;
    private final GetInstanceQrPort getInstanceQrUseCase;

    @PutMapping
    public ResponseEntity<ApiResponse<WhatsappInstanceResponseDTO>> upsert(
            @RequestBody @Valid WhatsappInstanceRequestDTO request
    ) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseUtil.ok(upsertInstanceUseCase.execute(request, tenantId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WhatsappInstanceResponseDTO>> get() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseUtil.ok(getInstanceUseCase.execute(tenantId));
    }

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<InstanceConnectResponseDTO>> connect() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseUtil.ok(connectInstanceUseCase.execute(tenantId));
    }

    @GetMapping("/qr")
    public ResponseEntity<ApiResponse<InstanceConnectResponseDTO>> getQr() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseUtil.ok(getInstanceQrUseCase.execute(tenantId));
    }
}
