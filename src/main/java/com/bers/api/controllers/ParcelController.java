package com.bers.api.controllers;

import com.bers.api.dtos.ParcelDtos.*;
import com.bers.domain.entities.enums.ParcelStatus;
import com.bers.services.service.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parcels")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
public class ParcelController {

    private final ParcelService parcelService;

    // ==================== CRUD BÁSICO ====================

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<ParcelResponse> createParcel(@Valid @RequestBody ParcelCreateRequest request) {
        log.info("Creating parcel from {} to {}", request.senderName(), request.receiverName());

        ParcelResponse created = parcelService.createParcel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ParcelResponse>> getAllParcels() {
        log.debug("Retrieving all parcels");

        List<ParcelResponse> parcels = parcelService.getAllParcels();
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> getParcelById(@PathVariable Long id) {
        log.debug("Retrieving parcel: {}", id);

        ParcelResponse parcel = parcelService.getParcelById(id);
        return ResponseEntity.ok(parcel);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ParcelResponse> getParcelByCode(@PathVariable String code) {
        log.debug("Retrieving parcel by code: {}", code);

        ParcelResponse parcel = parcelService.getParcelByCode(code);
        return ResponseEntity.ok(parcel);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<ParcelResponse> updateParcel(
            @PathVariable Long id,
            @Valid @RequestBody ParcelUpdateRequest request
    ) {
        log.info("Updating parcel: {}", id);

        ParcelResponse updated = parcelService.updateParcel(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteParcel(@PathVariable Long id) {
        log.warn("Deleting parcel: {}", id);

        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== GESTIÓN DE ESTADO ====================

    @PostMapping("/{id}/in-transit")
    public ResponseEntity<ParcelResponse> markAsInTransit(
            @PathVariable Long id,
            @RequestParam Long tripId
    ) {
        log.info("Marking parcel {} as in-transit for trip: {}", id, tripId);

        ParcelResponse updated = parcelService.markAsInTransit(id, tripId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/delivered")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<ParcelResponse> markAsDelivered(
            @PathVariable Long id,
            @RequestParam String otp,
            @RequestParam(required = false) String photoUrl
    ) {
        log.info("Marking parcel {} as delivered with OTP", id);

        ParcelResponse updated = parcelService.markAsDelivered(id, otp, photoUrl);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/failed")
    public ResponseEntity<ParcelResponse> markAsFailed(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        log.warn("Marking parcel {} as failed - reason: {}", id, reason);

        ParcelResponse updated = parcelService.markAsFailed(id, reason);
        return ResponseEntity.ok(updated);
    }

    // ==================== CONSULTAS ====================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParcelResponse>> getParcelsByStatus(@PathVariable ParcelStatus status) {
        log.debug("Retrieving parcels with status: {}", status);

        List<ParcelResponse> parcels = parcelService.getParcelsByStatus(status);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<ParcelResponse>> getParcelsByTrip(@PathVariable Long tripId) {
        log.debug("Retrieving parcels for trip: {}", tripId);

        List<ParcelResponse> parcels = parcelService.getParcelsByTripId(tripId);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<List<ParcelResponse>> getParcelsByPhone(@PathVariable String phone) {
        log.debug("Retrieving parcels for phone: {}", phone);

        List<ParcelResponse> parcels = parcelService.getParcelsByPhone(phone);
        return ResponseEntity.ok(parcels);
    }

    @PostMapping("/{id}/validate-otp")
    public ResponseEntity<Boolean> validateOtp(
            @PathVariable Long id,
            @RequestParam String otp
    ) {
        log.debug("Validating OTP for parcel: {}", id);

        boolean valid = parcelService.validateOtp(id, otp);
        return ResponseEntity.ok(valid);
    }
}