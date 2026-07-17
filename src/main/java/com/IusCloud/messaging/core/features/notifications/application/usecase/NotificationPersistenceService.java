package com.IusCloud.messaging.core.features.notifications.application.usecase;

import com.IusCloud.messaging.core.features.notifications.application.dto.CreateNotificationRequestDTO;
import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import com.IusCloud.messaging.core.features.notifications.domain.port.out.NotificationRepository;
import com.IusCloud.messaging.shared.enums.NotificationSender;
import com.IusCloud.messaging.shared.enums.NotificationStatus;
import com.IusCloud.messaging.shared.templates.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {

    private final NotificationRepository repository;
    private final TemplateRenderer templateRenderer;

    @Transactional
    public PersistResult persistPending(CreateNotificationRequestDTO request, UUID tenantId) {
        if (request.idempotencyKey() != null) {
            var existing = repository.findByTenantIdAndIdempotencyKey(tenantId, request.idempotencyKey());
            if (existing.isPresent()) {
                return new PersistResult(existing.get(), false);
            }
        }

        String rendered = templateRenderer.render(request.templateCode(), request.variables());

        if (rendered == null || rendered.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje renderizado está vacío. Verifica que las variables del template '" +
                    request.templateCode().name() + "' estén correctamente definidas.");
        }

        NotificationEntity entity = new NotificationEntity();
        entity.setTenantId(tenantId);
        entity.setRecipientPhone(request.recipientPhone());
        entity.setClientId(request.clientId());
        entity.setTemplateCode(request.templateCode().name());
        entity.setRenderedContent(rendered);
        entity.setScheduledAt(request.scheduledAt());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setPayload(request.payload());
        entity.setSender(request.sender() == null ? NotificationSender.TENANT : request.sender());

        boolean shouldSendNow = request.scheduledAt() == null || !request.scheduledAt().isAfter(Instant.now());
        entity.setStatus(shouldSendNow ? NotificationStatus.PENDING : NotificationStatus.SCHEDULED);

        NotificationEntity saved = repository.save(entity);
        return new PersistResult(saved, shouldSendNow);
    }

    public record PersistResult(NotificationEntity entity, boolean shouldSendNow) {}
}
