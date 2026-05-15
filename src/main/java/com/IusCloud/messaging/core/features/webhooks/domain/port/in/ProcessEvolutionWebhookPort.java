package com.IusCloud.messaging.core.features.webhooks.domain.port.in;

import java.util.Map;

public interface ProcessEvolutionWebhookPort {
    void execute(Map<String, Object> payload);
}
