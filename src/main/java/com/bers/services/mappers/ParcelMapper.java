package com.bers.services.mappers;

import com.bers.api.dtos.ParcelDtos.*;
import com.bers.domain.entities.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code",expression = "java(generateParcelCode())")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "senderPhone", source = "senderPhone")
    @Mapping(target = "receiverName", source = "receiverName")
    @Mapping(target = "receiverPhone", source = "receiverPhone")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "fromStop", source = "fromStopId", qualifiedByName = "mapStop")
    @Mapping(target = "toStop", source = "toStopId", qualifiedByName = "mapStop")
    @Mapping(target = "trip", source = "tripId", qualifiedByName = "mapTrip")
    @Mapping(target = "status", expression = "java(com.example.busconnect.domain.entities.enums.ParcelStatus.CREATED)")
    @Mapping(target = "proofPhotoUrl", ignore = true)
    @Mapping(target = "deliveryOtp", expression = "java(generateOTP())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    Parcel toEntity(ParcelCreateRequest dto);


    @Mapping(target = "status", source = "status")
    @Mapping(target = "proofPhotoUrl", source = "proofPhotoUrl")
    @Mapping(target = "deliveryOtp", source = "deliveryOtp")
    void updateEntity(ParcelUpdateRequest dto, @MappingTarget Parcel parcel);


    @Mapping(target = "fromStopId", source = "fromStop.id")
    @Mapping(target = "toStopId", source = "toStop.id")
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "status", source = "status")
    ParcelResponse toResponse(Parcel entity);

    @Named("mapStop")
    default Stop mapStop(Long id) {
        if (id == null) return null;
        Stop s = new Stop();
        s.setId(id);
        return s;
    }

    @Named("mapTrip")
    default Trip mapTrip(Long id) {
        if (id == null) return null;
        Trip t = new Trip();
        t.setId(id);
        return t;
    }

    default String generateParcelCode() {
        return "PCL-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    default String generateOTP() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
}
