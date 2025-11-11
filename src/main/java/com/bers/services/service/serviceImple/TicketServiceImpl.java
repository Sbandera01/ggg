package com.bers.services.service.serviceImple;

import com.bers.api.dtos.TicketDtos.*;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.CancellationPolicy;
import com.bers.domain.entities.enums.PassengerType;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.domain.repositories.*;
import com.bers.services.mappers.TicketMapper;
import com.bers.services.service.CancellationService;
import com.bers.services.service.DiscountService;
import com.bers.services.service.SegmentValidationService;
import com.bers.services.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final StopRepository stopRepository;
    private final FareRuleRepository fareRuleRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final TicketMapper ticketMapper;
    private final SegmentValidationService segmentValidationService;
    private final DiscountService discountService;
    private final CancellationService cancellationService;

    @Override
    public TicketResponse createTicket(TicketCreateRequest request) {
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + request.tripId()));

        User passenger = userRepository.findById(request.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found: " + request.passengerId()));

        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new IllegalArgumentException("From stop not found: " + request.fromStopId()));

        Stop toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new IllegalArgumentException("To stop not found: " + request.toStopId()));

        // Validar orden de paradas
        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid stop sequence: fromStop (%d) must be before toStop (%d)",
                    fromStop.getOrder(), toStop.getOrder()
            ));
        }

        segmentValidationService.validateSegment(
                trip.getId(),
                request.seatNumber(),
                fromStop.getOrder(),
                toStop.getOrder()
        );

        // Calcular precio
        BigDecimal basePrice = calculateFare(trip.getRoute().getId(),
                request.fromStopId(), request.toStopId());

        /*PassengerType passengerType = discountService.determinePassengerType(
          passenger.getAge(), passenger.getIsStudent()
        );*/
        PassengerType passengerType = discountService.determinePassengerType(
          passenger.getAge(), null);

        BigDecimal discount = discountService.calculateDiscount(passengerType, basePrice);
        BigDecimal finalPrice = basePrice.subtract(discount).max(BigDecimal.ZERO);

        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setTrip(trip);
        ticket.setPassenger(passenger);
        ticket.setFromStop(fromStop);
        ticket.setToStop(toStop);
        ticket.setPrice(finalPrice);
        ticket.setStatus(TicketStatus.SOLD);
        ticket.setDiscountAmount(discount);
        ticket.setCreatedAt(LocalDateTime.now());



        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("""
             Ticket creado exitosamente:
            ▸ ID: {}
            ▸ Pasajero: {}
            ▸ Tipo: {}
            ▸ Precio base: {}
            ▸ Descuento aplicado: {}
            ▸ Precio final: {}
            """,
                savedTicket.getId(), passenger.getUsername(),
                passengerType, basePrice, discount,
                finalPrice
        );
        return ticketMapper.toResponse(savedTicket);
    }

    @Override
    public TicketResponse updateTicket(Long id, TicketUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        ticketMapper.updateEntity(request, ticket);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updatedTicket);
    }

    @Override
    @Transactional
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse getTicketByQrCode(String qrCode) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with QR: " + qrCode));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketWithDetails(Long id) {
        Ticket ticket = ticketRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TicketResponse> getTicketsByTripId(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new IllegalArgumentException("Trip not found: " + tripId);
        }
        return ticketRepository.findByTripIdAndStatus(tripId, TicketStatus.SOLD).stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TicketResponse> getTicketsByPassengerId(Long passengerId) {
        if (!userRepository.existsById(passengerId)) {
            throw new IllegalArgumentException("Passenger not found: " + passengerId);
        }
        return ticketRepository.findByPassengerId(passengerId).stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TicketResponse> getTicketsByTripAndStatus(Long tripId, TicketStatus status) {
        if (!tripRepository.existsById(tripId)) {
            throw new IllegalArgumentException("Trip not found: " + tripId);
        }
        return ticketRepository.findByTripIdAndStatus(tripId, status).stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new IllegalArgumentException("Ticket not found: " + id);
        }
        ticketRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TicketResponse cancelTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Ticket already cancelled");
        }

        if (!cancellationService.canCancelTicket(ticket)) {
            String reason = cancellationService.getCancellationReason(ticket);
            throw new IllegalArgumentException("Cannot cancel ticket: " + reason);
        }

        BigDecimal refundAmount = cancellationService.calculateRefundAmount(ticket, LocalDateTime.now());
        CancellationPolicy policy = cancellationService.determineCancellationPolicy(ticket);

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCancelledAt(LocalDateTime.now());
        ticket.setRefundAmount(refundAmount);
        ticket.setCancellationPolicy(policy);

        Ticket updatedTicket = ticketRepository.save(ticket);

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            processRefund(ticket.getPassenger().getId(), refundAmount, ticket.getId());
        }
        log.info("""
            🎟️ Ticket cancelado exitosamente:
              • ID: {}
              • Pasajero: {}
              • Política: {}
              • Reembolso: {}
            """,
                id, ticket.getPassenger().getUsername(), policy, refundAmount
        );

        return ticketMapper.toResponse(updatedTicket);
    }

    @Override
    public TicketResponse markAsNoShow(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        if (ticket.getStatus() != TicketStatus.SOLD) {
            throw new IllegalArgumentException("Invalid ticket status for no-show");
        }

        ticket.setStatus(TicketStatus.NO_SHOW);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updatedTicket);
    }

    @Override
    public TicketResponse markAsUsed(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        if (ticket.getStatus() != TicketStatus.SOLD) {
            throw new IllegalArgumentException("Can only mark sold tickets as used");
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updatedTicket);
    }

    @Override
    @Transactional
    public boolean isSeatAvailable(Long tripId, String seatNumber) {
        // Verificar si hay un ticket vendido para ese asiento
        boolean ticketExists = ticketRepository.findSoldTicketBySeat(tripId, seatNumber).isPresent();

        // Verificar si hay un hold activo
        boolean holdExists = seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                tripId, seatNumber, com.example.busconnect.domine.entities.enums.HoldStatus.HOLD);

        return !ticketExists && !holdExists;
    }

    @Override
    @Transactional
    public long countSoldTicketsByTrip(Long tripId) {
        return ticketRepository.countSoldTicketsByTrip(tripId);
    }

    private BigDecimal calculateFare(Long routeId, Long fromStopId, Long toStopId) {
        return fareRuleRepository.findFareForSegment(routeId, fromStopId, toStopId)
                .map(FareRule::getBasePrice)
                .orElse(new BigDecimal("50000")); // Precio por defecto
    }

    private void processRefund(Long passengerId, BigDecimal amount, Long ticketId) {
        try {
            log.info(" Procesando reembolso de {} para pasajero {} (ticket {})",
                    amount, passengerId, ticketId);

        } catch (Exception ex) {
            log.error("Error al procesar reembolso para ticket {}: {}", ticketId, ex.getMessage());
            throw new IllegalStateException("Refund processing failed", ex);
        }
    }

}
