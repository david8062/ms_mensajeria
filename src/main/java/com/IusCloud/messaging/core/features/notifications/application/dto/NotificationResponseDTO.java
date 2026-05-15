package com.IusCloud.messaging.core.features.notifications.application.dto;

import com.IusCloud.messaging.core.base.BaseDTO;
import com.IusCloud.messaging.shared.enums.NotificationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class NotificationResponseDTO extends BaseDTO {
    private UUID tenantId;
    private String recipientPhone;
    private UUID clientId;
    private String templateCode;
    private String renderedContent;
    private NotificationStatus status;
    private String evolutionMessageId;
    private Instant scheduledAt;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;
    private String error;
    private Integer retryCount;
}
