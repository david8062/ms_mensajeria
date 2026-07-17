package com.IusCloud.messaging.core.features.notifications.application.usecase;

import com.IusCloud.messaging.core.common.evolution.EvolutionApiClient;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import com.IusCloud.messaging.core.features.instances.domain.port.out.WhatsappInstanceRepository;
import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import com.IusCloud.messaging.core.features.notifications.domain.port.in.DispatchNotificationPort;
import com.IusCloud.messaging.core.features.notifications.domain.port.out.NotificationRepository;
import com.IusCloud.messaging.shared.enums.NotificationSender;
import com.IusCloud.messaging.shared.enums.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchNotificationUseCase implements DispatchNotificationPort {

    private final NotificationRepository notificationRepository;
    private final WhatsappInstanceRepository instanceRepository;
    private final EvolutionApiClient evolutionApiClient;

    /** Línea propia de IusCloud: no vive en whatsapp_instances, se configura. */
    @Value("${messaging.platform.instance-name:IusCloud}")
    private String platformInstance;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(NotificationEntity notification) {
        String instanceName = resolveInstanceName(notification);

        if (instanceName == null) {
            markFailed(notification, "No hay instancia de WhatsApp configurada para el tenant");
            return;
        }

        try {
            EvolutionApiClient.SendMessageResult result = evolutionApiClient.sendText(
                    instanceName,
                    notification.getRecipientPhone(),
                    notification.getRenderedContent()
            );

            notification.setEvolutionMessageId(result.messageId());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setError(null);
            notificationRepository.save(notification);
        } catch (Exception ex) {
            log.warn("Notification {} failed to send: {}", notification.getId(), ex.getMessage());
            markFailed(notification, ex.getMessage());
        }
    }

    /**
     * @return el nombre de la instancia desde la que sale el mensaje, o {@code null} si
     *         el tenant debía tener una y no la ha conectado.
     */
    private String resolveInstanceName(NotificationEntity notification) {
        if (notification.getSender() == NotificationSender.PLATFORM) {
            return platformInstance;
        }
        return instanceRepository.findByTenantId(notification.getTenantId())
                .map(WhatsappInstanceEntity::getInstanceName)
                .orElse(null);
    }

    private void markFailed(NotificationEntity notification, String error) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setError(error);
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);
    }
}
