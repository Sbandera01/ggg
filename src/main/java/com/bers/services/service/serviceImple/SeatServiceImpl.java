package com.bers.services.service.serviceImple;

import com.bers.api.dtos.SeatDtos.*;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Seat;
import com.bers.domain.entities.enums.SeatType;
import com.bers.domain.repositories.BusRepository;
import com.bers.domain.repositories.SeatRepository;
import com.bers.services.mappers.SeatMapper;
import com.bers.services.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final BusRepository busRepository;
    private final SeatMapper seatMapper;

    @Override
    public SeatResponse createSeat(SeatCreateRequest request) {
        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + request.busId()));

        validateSeatNumber(request.busId(), request.number());

        long currentSeats = seatRepository.countByBusId(request.busId());
        if (currentSeats >= bus.getCapacity()) {
            throw new IllegalArgumentException("Bus capacity exceeded. Max capacity: " + bus.getCapacity());
        }

        Seat seat = seatMapper.toEntity(request);
        seat.setBus(bus);

        Seat savedSeat = seatRepository.save(seat);
        return seatMapper.toResponse(savedSeat);
    }

    @Override
    public SeatResponse updateSeat(Long id, SeatUpdateRequest request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + id));

        seatMapper.updateEntity(request, seat);
        Seat updatedSeat = seatRepository.save(seat);
        return seatMapper.toResponse(updatedSeat);
    }

    @Override
    @Transactional
    public SeatResponse getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + id));
        return seatMapper.toResponse(seat);
    }

    @Override
    @Transactional
    public SeatResponse getSeatByBusAndNumber(Long busId, String number) {
        Seat seat = seatRepository.findByBusIdAndNumber(busId, number)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat not found with number " + number + " in bus " + busId));
        return seatMapper.toResponse(seat);
    }

    @Override
    @Transactional
    public List<SeatResponse> getAllSeats() {
        return seatRepository.findAll().stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SeatResponse> getSeatsByBusId(Long busId) {
        if (!busRepository.existsById(busId)) {
            throw new IllegalArgumentException("Bus not found: " + busId);
        }
        return seatRepository.findByBusIdOrderByNumberAsc(busId).stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SeatResponse> getSeatsByBusIdAndType(Long busId, SeatType type) {
        if (!busRepository.existsById(busId)) {
            throw new IllegalArgumentException("Bus not found: " + busId);
        }
        return seatRepository.findByBusIdAndType(busId, type).stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSeat(Long id) {
        if (!seatRepository.existsById(id)) {
            throw new IllegalArgumentException("Seat not found: " + id);
        }
        seatRepository.deleteById(id);
    }

    @Override
    @Transactional
    public long countSeatsByBus(Long busId) {
        return seatRepository.countByBusId(busId);
    }

    @Override
    @Transactional
    public void validateSeatNumber(Long busId, String number) {
        if (seatRepository.findByBusIdAndNumber(busId, number).isPresent()) {
            throw new IllegalArgumentException(
                    "Seat number " + number + " already exists in bus " + busId);
        }
    }
}
