package com.bers.services.mappers;

import com.bers.api.dtos.IncidentDtos.*;
import com.bers.domain.entities.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "entityType", source = "entityType")
    @Mapping(target = "entityId", source = "entityId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "reportedBy", source = "reportedBy", qualifiedByName = "mapUser")
    @Mapping(target = "createdAt", ignore = true)
    Incident toEntity(IncidentCreateRequest dto);

    @Mapping(target = "note", source = "note")
    void updateEntity(IncidentUpdateRequest dto, @MappingTarget Incident entity);

    @Mapping(target = "entityType", source = "entityType")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "reportedBy", source = "reportedBy.id")
    @Mapping(target = "reportedByName", source = "reportedBy.username")
    IncidentResponse toResponse(Incident entity);

    @Named("mapUser")
    default User mapUser(Long id) {
        if (id == null) return null;
        User u = new User();
        u.setId(id);
        return u;
    }
}
