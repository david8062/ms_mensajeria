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
@Table(name = "webhook_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebhookEventEntity extends BaseModel {

    @Column(name = "instance_id")
    private UUID instanceId;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}
