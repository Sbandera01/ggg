package com.bers.api.dtos;

import com.bers.domain.entities.enums.SeatType;
import jakarta.validation.constraints.*;

import java.io.Serializable;
public class SeatDtos {
    public record SeatCreateRequest(
            @NotBlank(message = "number is required")
            @Size(max = 10, message = "number must not exceed 10 characters")
            String number,
            @NotNull(message = "type is required")
            SeatType type,
            @NotNull(message = "busId is required")
            Long busId
    ) implements Serializable {}
    public record SeatUpdateRequest(
            @NotNull(message = "type is required")
            SeatType type
    ) implements Serializable {}
    public record SeatResponse(
            Long id,
            String number,
            String type,
            Long busId,
            String busPlate,
            Integer busCapacity
    ) implements Serializable {}
}