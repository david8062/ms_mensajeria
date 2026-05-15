package com.IusCloud.messaging.core.features.notifications.application.scheduler;

import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import com.IusCloud.messaging.core.features.notifications.domain.port.in.DispatchNotificationPort;
import com.IusCloud.messaging.core.features.notifications.domain.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationDispatcher {

    private static final int BATCH_SIZE = 50;

    private final NotificationRepository repository;
    private final DispatchNotificationPort dispatcher;

    @Scheduled(fixedDelayString = "${messaging.scheduler.fixed-delay-ms:30000}")
    public void run() {
        List<NotificationEntity> due = repository.findDueScheduled(Instant.now(), BATCH_SIZE);
        if (due.isEmpty()) return;

        log.debug("Dispatching {} scheduled notifications", due.size());
        for (NotificationEntity n : due) {
            try {
                dispatcher.dispatch(n);
            } catch (Exception ex) {
                log.warn("Scheduled dispatch failed for notification {}: {}", n.getId(), ex.getMessage());
            }
        }
    }
}
