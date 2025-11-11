package com.bers.api.dtos;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class AssignmentDtos {
    public record AssignmentCreateRequest(
            @NotNull(message = "tripId is required")
            Long tripId,
            @NotNull(message = "driverId is required")
            Long driverId,
            @NotNull(message = "dispatcherId is required")
            Long dispatcherId
    ) implements Serializable {}
    public record AssignmentUpdateRequest(
            @NotNull(message = "checklistOk is required")
            Boolean checklistOk
    ) implements Serializable {}
    public record AssignmentResponse(
            Long id,
            Boolean checklistOk,
            LocalDateTime assignedAt,
            Long tripId,
            String tripInfo,
            String tripStatus,
            LocalDate tripDate,
            LocalDateTime tripDepartureTime,
            String routeInfo,
            Long driverId,
            String driverName,
            Long dispatcherId,
            String dispatcherName
    ) implements Serializable {}
}
