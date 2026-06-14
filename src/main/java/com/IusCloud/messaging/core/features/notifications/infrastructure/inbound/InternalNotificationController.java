package com.IusCloud.messaging.core.features.notifications.infrastructure.inbound;

import com.IusCloud.messaging.core.common.evolution.EvolutionApiClient;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.shared.exceptions.BusinessException;
import com.IusCloud.messaging.shared.responses.ApiResponse;
import com.IusCloud.messaging.shared.responses.ResponseUtil;
import com.IusCloud.messaging.shared.templates.NotificationTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final WhatsappInstanceRepository instanceRepository;
    private final EvolutionApiClient evolutionApiClient;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequestDTO request) {
        WhatsappInstanceEntity instance = instanceRepository.findByTenantId(request.getTenantId())
                .orElseThrow(() -> new BusinessException(
                        "INSTANCE_NOT_FOUND",
                        "No hay instancia de WhatsApp configurada para este tenant"
                ));

        String message = NotificationTemplate.OTP_VERIFICATION.getTemplate()
                .replace("{otp}", request.getOtp());

        evolutionApiClient.sendText(instance.getInstanceName(), request.getPhone(), message);

        return ResponseUtil.ok("OTP enviado correctamente");
    }
}
