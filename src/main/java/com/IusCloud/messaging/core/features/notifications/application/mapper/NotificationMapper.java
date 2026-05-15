package com.IusCloud.messaging.core.features.notifications.application.mapper;

import com.IusCloud.messaging.config.mapper.BaseEntityMapperConfig;
import com.IusCloud.messaging.core.features.notifications.application.dto.NotificationResponseDTO;
import com.IusCloud.messaging.core.features.notifications.domain.model.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = BaseEntityMapperConfig.class)
public interface NotificationMapper {
    NotificationResponseDTO toResponse(NotificationEntity entity);
}
