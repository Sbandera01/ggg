package com.bers.api.controllers;

import com.bers.api.dtos.AssignmentDtos.*;
import com.bers.api.dtos.TripDtos.*;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.AssignmentService;
import com.bers.services.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TripController {

    private final TripService tripService;
    private final AssignmentService assignmentService;

    /**
     *  CREAR VIAJE - Solo ADMIN y DISPATCHER
     */
    @PostMapping("create")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripCreateRequest request) {
        log.info("Creating new trip for route: {}, date: {}", request.routeId(), request.date());
        TripResponse response = tripService.createTrip(request);
        log.info("Trip created successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     *  ACTUALIZAR VIAJE - Solo ADMIN y DISPATCHER
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripUpdateRequest request) {
        log.info("Updating trip ID: {}", id);
        TripResponse response = tripService.updateTrip(id, request);
        log.info("Trip ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    /**
     *  OBTENER VIAJE POR ID
     */
    @GetMapping("/search/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        log.debug("Getting trip by ID: {}", id);
        TripResponse response = tripService.getTripById(id);
        return ResponseEntity.ok(response);
    }

    /**
     *  OBTENER VIAJE CON DETALLES
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<TripResponse> getTripWithDetails(@PathVariable Long id) {
        log.debug("Getting trip details for ID: {}", id);
        TripResponse response = tripService.getTripWithDetails(id);
        return ResponseEntity.ok(response);
    }

    /**
     *  OBTENER TODOS LOS VIAJES - SOLO ADMIN y DISPATCHER
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<TripResponse>> getAllTrips() {
        log.debug("Getting all trips");
        List<TripResponse> response = tripService.getAllTrips();
        return ResponseEntity.ok(response);
    }

    /**
     * BUSCAR VIAJES
     */
    @GetMapping("/search")
    public ResponseEntity<List<TripResponse>> searchTrips(
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) TripStatus status) {
        log.debug("Searching trips - routeId: {}, date: {}, status: {}", routeId, date, status);
        List<TripResponse> response = tripService.searchTrips(routeId, date, status);
        return ResponseEntity.ok(response);
    }

    /**
     * OBTENER VIAJES POR RUTA Y FECHA
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TripResponse>> getTripsByRouteAndDate(
            @PathVariable Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Getting trips for route: {} and date: {}", routeId, date);
        List<TripResponse> response = tripService.getTripsByRouteAndDate(routeId, date);
        return ResponseEntity.ok(response);
    }

    /**
     *  OBTENER VIAJES ACTIVOS POR BUS - SOLO ADMIN, DISPATCHER y DRIVER
     */
    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<TripResponse>> getActiveTripsByBus(
            @PathVariable Long busId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Getting active trips for bus: {} on date: {}", busId, date);
        List<TripResponse> response = tripService.getActiveTripsByBus(busId, date);
        return ResponseEntity.ok(response);
    }

    /**
     * CAMBIAR ESTADO DE VIAJE - DISPATCHER y DRIVER
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER')")
    public ResponseEntity<TripResponse> changeTripStatus(
            @PathVariable Long id,
            @RequestParam TripStatus status) {
        log.info("Changing trip ID: {} status to: {}", id, status);
        TripResponse response = tripService.changeTripStatus(id, status);
        log.info("Trip ID: {} status changed to: {}", id, status);
        return ResponseEntity.ok(response);
    }

    /**
     *  ABRIR ABORDAJE - SOLO DISPATCHER
     * SCHEDULED → BOARDING
     */
    @PostMapping("/{id}/boarding/open")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> openBoarding(@PathVariable Long id) {
        log.info("Opening boarding for trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.BOARDING);
        return ResponseEntity.ok(response);
    }

    /**
     * CERRAR ABORDAJE - SOLO DISPATCHER
     * BOARDING → DEPARTED
     */
    @PostMapping("/{id}/boarding/close")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> closeBoarding(@PathVariable Long id) {
        log.info("Closing boarding for trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.DEPARTED);
        return ResponseEntity.ok(response);
    }

    /**
     * MARCAR COMO PARTIDO - DRIVER y DISPATCHER
     * BOARDING → DEPARTED
     */
    @PostMapping("/{id}/depart")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER')")
    public ResponseEntity<TripResponse> markAsDeparted(@PathVariable Long id) {
        log.info("Marking trip ID: {} as departed", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.DEPARTED);
        return ResponseEntity.ok(response);
    }

    /**
     *  MARCAR COMO LLEGADO - DRIVER y DISPATCHER
     * DEPARTED → ARRIVED
     */
    @PostMapping("/{id}/arrive")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER')")
    public ResponseEntity<TripResponse> markAsArrived(@PathVariable Long id) {
        log.info("Marking trip ID: {} as arrived", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.ARRIVED);
        return ResponseEntity.ok(response);
    }

    /**
     *  CANCELAR VIAJE - SOLO DISPATCHER y ADMIN
     * Cualquier estado → CANCELLED
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<TripResponse> cancelTrip(@PathVariable Long id) {
        log.info("Canceling trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.CANCELLED);
        return ResponseEntity.ok(response);
    }

    /**
     *  REACTIVAR VIAJE CANCELADO - SOLO DISPATCHER y ADMIN
     * CANCELLED → SCHEDULED
     */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<TripResponse> reactivateTrip(@PathVariable Long id) {
        log.info("Reactivating cancelled trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.SCHEDULED);
        return ResponseEntity.ok(response);
    }

    /**
     *  ELIMINAR VIAJE - SOLO ADMIN
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        log.info("Deleting trip ID: {}", id);
        tripService.deleteTrip(id);
        log.info("Trip ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    /**
     *  OBTENER VIAJES DEL DÍA ACTUAL
     */
    @GetMapping("/today")
    public ResponseEntity<List<TripResponse>> getTodayTrips() {
        log.debug("Getting today's trips");
        List<TripResponse> response = tripService.getTripsByRouteAndDate(null, LocalDate.now());
        return ResponseEntity.ok(response);
    }

    /**
     * OBTENER VIAJES POR ESTADO - SOLO ADMIN y DISPATCHER
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<TripResponse>> getTripsByStatus(@PathVariable TripStatus status) {
        log.debug("Getting trips by status: {}", status);
        List<TripResponse> response = tripService.searchTrips(null, null, status);
        return ResponseEntity.ok(response);
    }

    /**
     * OBTENER VIAJES ACTIVOS DEL DÍA
     * Viajes que no están CANCELLED ni ARRIVED del día actual
     */
    @GetMapping("/today/active")
    public ResponseEntity<List<TripResponse>> getTodayActiveTrips() {
        log.debug("Getting today's active trips");
        List<TripResponse> response = tripService.searchTrips(null, LocalDate.now(), null);
        return ResponseEntity.ok(response);
    }

    /**
     * OBTENER VIAJES ASIGNADOS PARA UN CONDUCTOR - SOLO DRIVER y superiores
     */
    @GetMapping("/driver/my-trips")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> getDriverTrips(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        log.debug("Getting driver trips for date: {}", date);

        try {
            // Obtener el ID del conductor autenticado
            Long driverId = getCurrentDriverId(authentication);

            List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDriverAndDate(driverId, date);

            log.debug("Found {} assignments for driver {} on date {}",
                    assignments.size(), driverId, date);
            return ResponseEntity.ok(assignments);

        } catch (IllegalArgumentException e) {
            log.warn("Error getting driver trips: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error getting driver trips", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * OBTENER VIAJES ASIGNADOS ACTUALES DEL CONDUCTOR AUTENTICADO
     */
    @GetMapping("/driver/current-trips")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> getCurrentDriverTrips(Authentication authentication) {
        log.debug("Getting current driver trips");

        try {
            Long driverId = getCurrentDriverId(authentication);
            LocalDate today = LocalDate.now();

            List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDriverAndDate(driverId, today);

            log.debug("Found {} current assignments for driver {}", assignments.size(), driverId);
            return ResponseEntity.ok(assignments);

        } catch (IllegalArgumentException e) {
            log.warn("Error getting current driver trips: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error getting current driver trips", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * OBTENER VIAJES ACTIVOS ASIGNADOS AL CONDUCTOR
     * Viajes que están en estado SCHEDULED o BOARDING
     */
    @GetMapping("/driver/active-trips")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> getActiveDriverTrips(Authentication authentication) {
        log.debug("Getting active driver trips");

        try {
            Long driverId = getCurrentDriverId(authentication);

            List<AssignmentResponse> assignments = assignmentService.getActiveAssignmentsByDriver(driverId);

            log.debug("Found {} active assignments for driver {}", assignments.size(), driverId);
            return ResponseEntity.ok(assignments);

        } catch (IllegalArgumentException e) {
            log.warn("Error getting active driver trips: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error getting active driver trips", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Metodo auxiliar para obtener el ID del conductor autenticado
     */
    private Long getCurrentDriverId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            // Verificar que el usuario sea un conductor
            if (!userDetails.hasRole("DRIVER")) {
                throw new IllegalArgumentException("User is not a driver");
            }
            return userDetails.getId();
        }

        throw new IllegalArgumentException("Unable to get driver ID from authentication");
    }
}