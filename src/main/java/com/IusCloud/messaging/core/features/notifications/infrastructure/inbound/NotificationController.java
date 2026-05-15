package com.IusCloud.messaging.core.features.notifications.infrastructure.inbound;

import com.IusCloud.messaging.core.features.notifications.application.dto.CreateNotificationRequestDTO;
import com.IusCloud.messaging.core.features.notifications.application.dto.NotificationResponseDTO;
import com.IusCloud.messaging.core.features.notifications.domain.port.in.CreateNotificationPort;
import com.IusCloud.messaging.core.features.notifications.domain.port.in.GetNotificationByIdPort;
import com.IusCloud.messaging.shared.responses.ApiResponse;
import com.IusCloud.messaging.shared.responses.ResponseUtil;
import com.IusCloud.messaging.shared.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final CreateNotificationPort createNotificationUseCase;
    private final GetNotificationByIdPort getNotificationByIdUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> create(
            @RequestBody @Valid CreateNotificationRequestDTO request
    ) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseUtil.created(createNotificationUseCase.execute(request, tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> getById(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseUtil.ok(getNotificationByIdUseCase.execute(id, tenantId));
    }
}
