package com.IusCloud.messaging.core.features.assistant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Cliente hacia el asistente de ms-ia (X-Internal-Key). Le pasa el mensaje entrante y recibe el
 * texto a responder por WhatsApp. El "cerebro" (identidad + IA + datos) vive en ms-ia; acá solo
 * se hace la entrada/salida de WhatsApp.
 */
@Component
@Slf4j
public class AssistantClient {

    private final RestClient client;
    private final String internalApiKey;

    public AssistantClient(
            @Value("${ai.service.url:http://iuscloud-ms-ia:8087/ms-ia}") String baseUrl,
            @Value("${internal.api.key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        // El asistente hace una llamada a Claude: puede tardar. Damos margen amplio.
        factory.setReadTimeout((int) Duration.ofSeconds(45).toMillis());

        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /** Devuelve el texto a enviar, o {@code null} si ms-ia no respondió. */
    public String requestReply(String phone, String text) {
        try {
            AssistantReply reply = client.post()
                    .uri("/api/v1/internal/assistant/whatsapp")
                    .header("X-Internal-Key", internalApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("phone", phone, "text", text))
                    .retrieve()
                    .body(AssistantReply.class);
            return reply != null ? reply.reply() : null;
        } catch (Exception e) {
            log.warn("El asistente (ms-ia) no respondió para {}: {}", phone, e.getMessage());
            return null;
        }
    }

    private record AssistantReply(String reply) {}
}
