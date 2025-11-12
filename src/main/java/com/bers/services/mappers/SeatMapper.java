package com.bers.services.mappers;

import com.bers.api.dtos.SeatDtos.*;
import com.bers.domain.entities.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", source = "number")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "bus", source = "busId", qualifiedByName = "mapBus")
    Seat toEntity(SeatCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "type", source = "type")
    void updateEntity(SeatUpdateRequest dto, @MappingTarget Seat seat);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "busId", source = "bus.id")
    @Mapping(target = "busPlate", source = "bus.plate")
    @Mapping(target = "busCapacity", source = "bus.capacity")
    SeatResponse toResponse(Seat entity);

    @Named("mapBus")
    default Bus mapBus(Long id) {
        if (id == null) return null;
        Bus b = new Bus();
        b.setId(id);
        return b;
    }
}