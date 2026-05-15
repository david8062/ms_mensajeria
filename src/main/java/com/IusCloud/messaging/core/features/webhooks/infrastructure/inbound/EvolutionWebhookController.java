package com.IusCloud.messaging.core.features.webhooks.infrastructure.inbound;

import com.IusCloud.messaging.core.features.webhooks.domain.port.in.ProcessEvolutionWebhookPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/evolution")
@RequiredArgsConstructor
@Slf4j
public class EvolutionWebhookController {

    private final ProcessEvolutionWebhookPort processWebhook;

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        try {
            processWebhook.execute(payload);
        } catch (Exception ex) {
            log.error("Unhandled error processing Evolution webhook: {}", ex.getMessage(), ex);
        }
        return ResponseEntity.ok().build();
    }
}
