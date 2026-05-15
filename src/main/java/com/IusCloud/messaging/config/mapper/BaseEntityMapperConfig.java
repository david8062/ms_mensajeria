package com.IusCloud.messaging.config.mapper;


import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@MapperConfig(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BaseEntityMapperConfig {

    // Configuración para Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void baseToEntityMapping(Object source, @org.mapstruct.MappingTarget Object target);
}
