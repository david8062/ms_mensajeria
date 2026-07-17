package com.IusCloud.messaging.core.features.notifications.domain.model;

import com.IusCloud.messaging.core.base.BaseModel;
import com.IusCloud.messaging.shared.enums.NotificationSender;
import com.IusCloud.messaging.shared.enums.NotificationStatus;
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
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "recipient_phone", nullable = false, length = 30)
    private String recipientPhone;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "template_code", nullable = false, length = 60)
    private String templateCode;

    @Column(name = "rendered_content", nullable = false, columnDefinition = "TEXT")
    private String renderedContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 20)
    private NotificationSender sender = NotificationSender.TENANT;

    @Column(name = "evolution_message_id", length = 120)
    private String evolutionMessageId;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;
}
