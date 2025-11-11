package com.bers.services.service;

import com.bers.api.dtos.RouteDtos.*;
import com.bers.api.dtos.StopDtos.StopResponse;

import java.util.List;
public interface RouteService {

    RouteResponse createRoute(RouteCreateRequest request);

    RouteResponse updateRoute(Long id, RouteUpdateRequest request);

    RouteResponse getRouteById(Long id);

    RouteResponse getRouteByCode(String code);

    RouteResponse getRouteWithStops(Long id);

    List<RouteResponse> getAllRoutes();

    List<RouteResponse> searchRoutes(String origin, String destination);

    List<StopResponse> getStopsByRoute(Long routeId);

    void deleteRoute(Long id);

    boolean existsByCode(String code);
}
