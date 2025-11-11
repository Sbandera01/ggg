package com.bers.services.mappers;

import com.bers.api.dtos.FareRuleDtos.*;
import com.bers.domain.entities.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "discounts", source = "discounts")
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    @Mapping(target = "route", source = "routeId", qualifiedByName = "mapRoute")
    @Mapping(target = "fromStop", source = "fromStopId", qualifiedByName = "mapStop")
    @Mapping(target = "toStop", source = "toStopId", qualifiedByName = "mapStop")
    FareRule toEntity(FareRuleCreateRequest dto);


    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "discounts", source = "discounts")
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    void updateEntity(FareRuleUpdateRequest dto, @MappingTarget FareRule entity);

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "fromStopId", source = "fromStop.id")
    @Mapping(target = "toStopId", source = "toStop.id")
    @Mapping(target = "fromStopName", source = "fromStop.name")
    @Mapping(target = "toStopName", source = "toStop.name")
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    FareRuleResponse toResponse(FareRule entity);

    @Named("mapRoute")
    default Route mapRoute(Long id) {
        if (id == null) return null;
        Route r = new Route();
        r.setId(id);
        return r;
    }
    @Named("mapStop")
    default Stop mapStop(Long id) {
        if (id == null) return null;
        Stop s = new Stop();
        s.setId(id);
        return s;
    }
}