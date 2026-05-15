package com.IusCloud.messaging.core.features.instances.application.mapper;

import com.IusCloud.messaging.config.mapper.BaseEntityMapperConfig;
import com.IusCloud.messaging.core.features.instances.application.dto.WhatsappInstanceResponseDTO;
import com.IusCloud.messaging.core.features.instances.domain.model.WhatsappInstanceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = BaseEntityMapperConfig.class)
public interface WhatsappInstanceMapper {
    WhatsappInstanceResponseDTO toResponse(WhatsappInstanceEntity entity);
}
