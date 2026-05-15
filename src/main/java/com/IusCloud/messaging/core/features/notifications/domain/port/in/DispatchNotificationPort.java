package com.IusCloud.messaging.core.features.notifications.domain.port.in;

import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;

public interface DispatchNotificationPort {
    void dispatch(NotificationEntity notification);
}
