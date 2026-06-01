package com.IusCloud.messaging.core.common.evolution;

import com.IusCloud.messaging.shared.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class EvolutionApiClient {

    private final RestClient client;

    public EvolutionApiClient(
            @Value("${evolution.api.url}") String baseUrl,
            @Value("${evolution.api.key:}") String apiKey
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory);

        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("apikey", apiKey);
        }
        builder.defaultHeader("Accept", "application/json");
        this.client = builder.build();
    }

    public SendMessageResult sendText(String instanceName, String phoneNumber, String content) {
        Map<String, Object> body = Map.of(
                "number", phoneNumber,
                "text", content
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                    .uri("/message/sendText/{instance}", instanceName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new BusinessException("Evolution API respondió " + res.getStatusCode() + ": "
                                + new String(res.getBody().readAllBytes()));
                    })
                    .body(Map.class);

            String messageId = extractMessageId(response);
            return new SendMessageResult(messageId, response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Evolution sendText failed for instance={} phone={}: {}", instanceName, phoneNumber, e.getMessage());
            throw new BusinessException("Error enviando mensaje vía Evolution: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<String, Object> response) {
        if (response == null) return null;
        Object key = response.get("key");
        if (key instanceof Map<?, ?> keyMap) {
            Object id = ((Map<String, Object>) keyMap).get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    public record SendMessageResult(String messageId, Map<String, Object> raw) {}
}
