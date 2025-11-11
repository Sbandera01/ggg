package com.bers.services.service;

import com.bers.api.dtos.TripDtos.*;
import com.bers.domain.entities.enums.TripStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
public interface TripService {
    TripResponse createTrip(TripCreateRequest request);
    TripResponse updateTrip(Long id, TripUpdateRequest request);
    TripResponse getTripById(Long id);
    TripResponse getTripWithDetails(Long id);
    List<TripResponse> getAllTrips();
    List<TripResponse> getTripsByRouteAndDate(Long routeId, LocalDate date);
    List<TripResponse> getTripsByRouteAndDateAndStatus(Long routeId, LocalDate date, TripStatus status);
    List<TripResponse> searchTrips(Long routeId, LocalDate date, TripStatus status);
    List<TripResponse> getTripsByDateAndStatus(LocalDate date, TripStatus status);
    List<TripResponse> getActiveTripsByBus(Long busId, LocalDate date);
    void deleteTrip(Long id);
    TripResponse changeTripStatus(Long id, TripStatus status);
    void validateTripSchedule(Long busId, LocalDate date, LocalDateTime departureAt);
}
