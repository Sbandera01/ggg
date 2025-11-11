package com.bers.services.mappers;
import com.bers.api.dtos.ConfigDtos.*;
import com.bers.domain.entities.Config;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "key", source = "key")
    @Mapping(target = "value", source = "value")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "updatedAt", ignore = true)
    Config toEntity(ConfigCreateRequest dto);


    @Mapping(target = "value", source = "value")
    @Mapping(target = "description", source = "description")
    void updateEntity(ConfigUpdateRequest dto, @MappingTarget Config config);

    ConfigResponse toResponse(Config entity);
}