package com.IusCloud.messaging.core.features.webhooks.domain.model;

import com.IusCloud.messaging.core.base.BaseModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_inbound_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WhatsappInboundMessageEntity extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "instance_id", nullable = false)
    private UUID instanceId;

    @Column(name = "sender_phone", nullable = false, length = 30)
    private String senderPhone;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "media_type", length = 30)
    private String mediaType;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "evolution_message_id", length = 120)
    private String evolutionMessageId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private Map<String, Object> rawPayload;
}
