package com.bers.services.mappers;
import com.bers.api.dtos.StopDtos.*;
import com.bers.domain.entities.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StopMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "order", source = "order")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lng", source = "lng")
    @Mapping(target = "route", source = "routeId", qualifiedByName = "mapRoute")
    Stop toEntity(StopCreateRequest dto);


    @Mapping(target = "name", source = "name")
    @Mapping(target = "order", source = "order")
    void updateEntity(StopUpdateRequest dto, @MappingTarget Stop stop);



    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "routeName", source = "route.name")
    @Mapping(target = "routeCode", source = "route.code")
    StopResponse toResponse(Stop entity);
    @Named("mapRoute")
    default Route mapRoute(Long id) {
        if (id == null) return null;
        Route r = new Route();
        r.setId(id);
        return r;
    }
}
