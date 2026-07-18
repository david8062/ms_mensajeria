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

    /**
     * Envía un archivo (documento) por WhatsApp. {@code mediaUrl} es una URL que Evolution
     * descarga (presigned de MinIO). Se usa para mandarle al abogado los documentos de su caso.
     */
    public SendMessageResult sendMedia(String instanceName, String phoneNumber, String mediaUrl,
                                       String fileName, String mimeType) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("number", phoneNumber);
        body.put("mediatype", "document");
        body.put("media", mediaUrl);
        // Sin mimetype WhatsApp entrega el archivo como adjunto genérico que no se puede abrir.
        if (mimeType != null && !mimeType.isBlank()) {
            body.put("mimetype", mimeType);
        }
        String finalName = ensureExtension(fileName, mimeType);
        if (finalName != null && !finalName.isBlank()) {
            body.put("fileName", finalName);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                    .uri("/message/sendMedia/{instance}", instanceName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new BusinessException("Evolution API respondió " + res.getStatusCode() + ": "
                                + new String(res.getBody().readAllBytes()));
                    })
                    .body(Map.class);

            return new SendMessageResult(extractMessageId(response), response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Evolution sendMedia failed for instance={} phone={}: {}", instanceName, phoneNumber, e.getMessage());
            throw new BusinessException("Error enviando archivo vía Evolution: " + e.getMessage());
        }
    }

    /**
     * WhatsApp usa la extensión del nombre para saber con qué app abrir el archivo. El nombre visible
     * del documento (p. ej. "Contestación demanda") suele venir sin extensión; si falta, la derivamos
     * del mimeType para que el archivo se pueda abrir en el teléfono.
     */
    static String ensureExtension(String fileName, String mimeType) {
        if (fileName == null || fileName.isBlank()) {
            return fileName;
        }
        String name = fileName.trim();
        int lastDot = name.lastIndexOf('.');
        // Ya tiene una extensión razonable (1-5 chars tras el punto): no tocar.
        if (lastDot > 0 && lastDot >= name.length() - 6) {
            return name;
        }
        String ext = extensionFor(mimeType);
        return ext == null ? name : name + "." + ext;
    }

    /** Extensión (sin punto) para los tipos que sube el abogado; {@code null} si no la conocemos. */
    private static String extensionFor(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        return switch (mimeType.trim().toLowerCase().split(";")[0]) {
            case "application/pdf" -> "pdf";
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "application/msword" -> "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "application/vnd.ms-excel" -> "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
            case "text/plain" -> "txt";
            default -> null;
        };
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

    public InstanceConnectResult connectInstance(String instanceName) {
        int maxAttempts = 5;
        long delayMs = 1_500;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            InstanceConnectResult result = fetchQr(instanceName);
            if (result.base64Qr() != null) {
                return result;
            }
            if (attempt < maxAttempts) {
                log.debug("QR no listo para instance={}, reintento {}/{}", instanceName, attempt, maxAttempts);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.warn("QR no disponible para instance={} tras {} intentos", instanceName, maxAttempts);
        return new InstanceConnectResult(null, null, Map.of());
    }

    @SuppressWarnings("unchecked")
    public InstanceConnectResult fetchQr(String instanceName) {
        try {
            Map<String, Object> response = client.get()
                    .uri("/instance/connect/{instance}", instanceName)
                    .retrieve()
                    .onStatus(s -> s.value() == 404, (req, res) -> {
                        throw new BusinessException("INSTANCE_NOT_IN_EVOLUTION",
                                "La instancia no existe en Evolution API. Registra la instancia primero con PUT /instances.");
                    })
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new BusinessException("Evolution API respondió " + res.getStatusCode() + " al obtener QR");
                    })
                    .body(Map.class);

            if (response == null) throw new BusinessException("Evolution no devolvió datos de conexión");

            String base64 = stringVal(response.get("base64"));
            if (base64 == null) {
                log.warn("Evolution devolvió base64=null para instance={}, respuesta completa: {}", instanceName, response);
            }
            return new InstanceConnectResult(base64, stringVal(response.get("code")), response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Evolution fetchQr failed for instance={}: {}", instanceName, e.getMessage());
            throw new BusinessException("Error obteniendo QR de Evolution: " + e.getMessage());
        }
    }

    public void ensureInstance(String instanceName) {
        Map<String, Object> body = Map.of(
                "instanceName", instanceName,
                "qrcode", true,
                "integration", "WHATSAPP-BAILEYS"
        );
        try {
            client.post()
                    .uri("/instance/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(
                            s -> s.isError() && s.value() != 400 && s.value() != 409,
                            (req, res) -> {
                                throw new BusinessException("Error registrando instancia en Evolution: " + res.getStatusCode());
                            }
                    )
                    .toBodilessEntity();
            log.info("Evolution: instancia {} registrada o ya existía", instanceName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Evolution ensureInstance failed for {}: {}", instanceName, e.getMessage());
            throw new BusinessException("Error registrando instancia en Evolution: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public String getConnectionState(String instanceName) {
        try {
            Map<String, Object> response = client.get()
                    .uri("/instance/connectionState/{instance}", instanceName)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new BusinessException("Evolution API respondió " + res.getStatusCode() + " al consultar estado");
                    })
                    .body(Map.class);

            if (response == null) return "unknown";
            Object instanceObj = response.get("instance");
            if (instanceObj instanceof Map<?, ?> m) {
                Object state = ((Map<String, Object>) m).get("state");
                return state != null ? state.toString() : "unknown";
            }
            return "unknown";
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Evolution getConnectionState failed for instance={}: {}", instanceName, e.getMessage());
            throw new BusinessException("Error consultando estado en Evolution: " + e.getMessage());
        }
    }

    private static String stringVal(Object o) {
        return o != null ? o.toString() : null;
    }

    public record SendMessageResult(String messageId, Map<String, Object> raw) {}

    public record InstanceConnectResult(String base64Qr, String pairingCode, Map<String, Object> raw) {}
}
