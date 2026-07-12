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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final WhatsappInstanceRepository instanceRepository;
    private final EvolutionApiClient evolutionApiClient;

    /** Línea propia de IusCloud, la única que puede escribirle a alguien que aún no es cliente. */
    @Value("${messaging.platform.instance-name:IusCloud}")
    private String platformInstance;

    /**
     * OTP de verificación de un PROSPECTO (registro). No puede usar {@code /send-otp}: ese
     * envía desde la instancia del tenant, y en el registro el tenant todavía no existe.
     * Sale desde la instancia de plataforma.
     */
    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<String>> sendVerification(
            @Valid @RequestBody SendVerificationRequestDTO request) {

        String message = NotificationTemplate.SIGNUP_VERIFICATION.getTemplate()
                .replace("{otp}", request.getOtp());

        evolutionApiClient.sendText(platformInstance, request.getPhone(), message);

        return ResponseUtil.ok("Código de verificación enviado");
    }

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
