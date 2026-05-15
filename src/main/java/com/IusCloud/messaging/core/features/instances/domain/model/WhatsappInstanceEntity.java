package com.IusCloud.messaging.core.features.instances.domain.model;

import com.IusCloud.messaging.core.base.BaseModel;
import com.IusCloud.messaging.shared.enums.WhatsappInstanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_instances")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WhatsappInstanceEntity extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "instance_name", nullable = false, length = 120)
    private String instanceName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WhatsappInstanceStatus status = WhatsappInstanceStatus.DISCONNECTED;

    @Column(name = "webhook_secret", length = 120)
    private String webhookSecret;

    @Column(name = "last_connected_at")
    private Instant lastConnectedAt;
}
