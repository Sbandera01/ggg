package com.bers.services.mappers;

import com.bers.api.dtos.TripDtos.*;
import com.bers.domain.entities.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", source = "date")
    @Mapping(target = "departureAt", source = "departureAt")
    @Mapping(target = "arrivalEta", source = "arrivalEta")
    @Mapping(target = "route", source = "routeId", qualifiedByName = "mapRoute")
    @Mapping(target = "bus", source = "busId", qualifiedByName = "mapBus")
    @Mapping(target = "status", expression = "java(com.bers.domain.entities.enums.TripStatus.SCHEDULED)")
    Trip toEntity(TripCreateRequest dto);


    @Mapping(target = "departureAt", source = "departureAt")
    @Mapping(target = "arrivalEta", source = "arrivalEta")
    @Mapping(target = "bus", source = "busId", qualifiedByName = "mapBus")
    @Mapping(target = "status", source = "status")
    void updateEntity(TripUpdateRequest dto, @MappingTarget Trip entity);

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "routeName", source = "route.name")
    @Mapping(target = "origin", source = "route.origin")
    @Mapping(target = "destination", source = "route.destination")
    @Mapping(target = "busId", source = "bus.id")
    @Mapping(target = "busPlate", source = "bus.plate")
    @Mapping(target = "capacity", source = "bus.capacity")
    TripResponse toResponse(Trip entity);

    @Named("mapRoute")
    default Route mapRoute(Long id) {
        if (id == null) return null;
        Route r = new Route();
        r.setId(id);
        return r;
    }

    @Named("mapBus")
    default Bus mapBus(Long id) {
        if (id == null) return null;
        Bus b = new Bus();
        b.setId(id);
        return b;
    }
}

