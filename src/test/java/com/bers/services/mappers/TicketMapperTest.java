package com.bers.services.mappers;

import com.bers.api.dtos.TicketDtos.*;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("TicketMapper Tests")
class TicketMapperTest {
    private TicketMapper ticketMapper;
    @BeforeEach
    void setUp() {
        ticketMapper = Mappers.getMapper(TicketMapper.class);
    }

    @Test
    @DisplayName("Debe mapear TicketCreateRequest a la entidad Ticket")
    void shouldMapCreateRequestToEntity() {
        TicketCreateRequest request = new TicketCreateRequest(
                1L,
                2L,
                3L,
                4L,
                "1A",
                PaymentMethod.CASH
        );

        Ticket ticket = ticketMapper.toEntity(request);

        assertNotNull(ticket);
        assertEquals("1A", ticket.getSeatNumber());
        assertEquals(PaymentMethod.CASH, ticket.getPaymentMethod());
        assertEquals(TicketStatus.SOLD, ticket.getStatus());
        assertNotNull(ticket.getQrCode());
        assertTrue(ticket.getQrCode().startsWith("TICKET-1-1A-"));
        assertNull(ticket.getId());
        assertNull(ticket.getPrice());
    }

    @Test
    @DisplayName("Debe generar códigos QR únicos")
    void shouldGenerateUniqueQrCodes() {
        TicketCreateRequest request = new TicketCreateRequest(
                1L, 2L, 3L, 4L, "1A", PaymentMethod.CASH
        );

        Ticket ticket1 = ticketMapper.toEntity(request);
        Ticket ticket2 = ticketMapper.toEntity(request);

        assertNotEquals(ticket1.getQrCode(), ticket2.getQrCode());
    }

    @Test
    @DisplayName("Debe actualizar la entidad Ticket desde TicketUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        Ticket existingTicket = Ticket.builder()
                .id(1L)
                .seatNumber("1A")
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-123")
                .build();

        TicketUpdateRequest request = new TicketUpdateRequest(
                TicketStatus.CANCELLED
        );

        ticketMapper.updateEntity(request, existingTicket);

        assertEquals(TicketStatus.CANCELLED, existingTicket.getStatus());
        assertEquals("1A", existingTicket.getSeatNumber());
        assertEquals(new BigDecimal("50000"), existingTicket.getPrice());
    }

    @Test
    @DisplayName("Debe mapear la entidad Ticket a TicketResponse")
    void shouldMapEntityToResponse() {
        Route route = Route.builder()
                .id(1L)
                .name("Bogotá - Tunja")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 12, 25))
                .departureAt(LocalDateTime.of(2025, 12, 25, 8, 0))
                .route(route)
                .build();

        User passenger = User.builder()
                .id(2L)
                .username("John Doe")
                .build();

        Stop fromStop = Stop.builder()
                .id(3L)
                .name("Terminal Bogotá")
                .build();

        Stop toStop = Stop.builder()
                .id(4L)
                .name("Terminal Tunja")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .fromStop(fromStop)
                .toStop(toStop)
                .seatNumber("1A")
                .price(new BigDecimal("45000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-123-456")
                .createdAt(createdAt)
                .build();

        TicketResponse response = ticketMapper.toResponse(ticket);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(1L, response.tripId());
        assertEquals("2025-12-25", response.tripDate());
        assertEquals("08:00", response.tripTime());
        assertEquals(2L, response.passengerId());
        assertEquals("John Doe", response.passengerName());
        assertEquals(3L, response.fromStopId());
        assertEquals("Terminal Bogotá", response.fromStopName());
        assertEquals(4L, response.toStopId());
        assertEquals("Terminal Tunja", response.toStopName());
        assertEquals("1A", response.seatNumber());
        assertEquals(new BigDecimal("45000"), response.price());
        assertEquals("CARD", response.paymentMethod());
        assertEquals("SOLD", response.status());
        assertEquals("QR-123-456", response.qrCode());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    @DisplayName("Debe formatear la fecha correctamente")
    void shouldFormatDateCorrectly() {
        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 1, 5))
                .departureAt(LocalDateTime.of(2025, 1, 5, 14, 30))
                .route(Route.builder().id(1L).build())
                .build();

        Ticket ticket = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(User.builder().id(1L).username("Test").build())
                .fromStop(Stop.builder().id(1L).name("A").build())
                .toStop(Stop.builder().id(2L).name("B").build())
                .seatNumber("1A")
                .price(BigDecimal.TEN)
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR")
                .createdAt(LocalDateTime.now())
                .build();

        TicketResponse response = ticketMapper.toResponse(ticket);

        assertEquals("2025-01-05", response.tripDate());
        assertEquals("14:30", response.tripTime());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de PaymentMethod correctamente")
    void shouldMapAllPaymentMethodTypes() {
        for (PaymentMethod method : PaymentMethod.values()) {
            TicketCreateRequest request = new TicketCreateRequest(
                    1L, 2L, 3L, 4L, "1A", method
            );

            Ticket ticket = ticketMapper.toEntity(request);

            assertEquals(method, ticket.getPaymentMethod());

            ticket.setId(1L);
            ticket.setPrice(BigDecimal.TEN);
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setTrip(Trip.builder().id(1L)
                    .date(LocalDate.now())
                    .departureAt(LocalDateTime.now())
                    .route(Route.builder().id(1L).build())
                    .build());
            ticket.setPassenger(User.builder().id(1L).username("Test").build());
            ticket.setFromStop(Stop.builder().id(1L).name("A").build());
            ticket.setToStop(Stop.builder().id(2L).name("B").build());

            TicketResponse response = ticketMapper.toResponse(ticket);
            assertEquals(method.name(), response.paymentMethod());
        }
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de TicketStatus correctamente")
    void shouldMapAllTicketStatusTypes() {
        for (TicketStatus status : TicketStatus.values()) {
            Ticket ticket = Ticket.builder()
                    .id(1L)
                    .trip(Trip.builder().id(1L)
                            .date(LocalDate.now())
                            .departureAt(LocalDateTime.now())
                            .route(Route.builder().id(1L).build())
                            .build())
                    .passenger(User.builder().id(1L).username("Test").build())
                    .fromStop(Stop.builder().id(1L).name("A").build())
                    .toStop(Stop.builder().id(2L).name("B").build())
                    .seatNumber("1A")
                    .price(BigDecimal.TEN)
                    .paymentMethod(PaymentMethod.CASH)
                    .status(status)
                    .qrCode("QR")
                    .createdAt(LocalDateTime.now())
                    .build();

            TicketResponse response = ticketMapper.toResponse(ticket);

            assertEquals(status.name(), response.status());
        }
    }

    @Test
    @DisplayName("Debe manejar la generación de códigos QR para diferentes viajes")
    void shouldHandleQrCodeGenerationForDifferentTrips() {
        TicketCreateRequest request1 = new TicketCreateRequest(
                1L, 2L, 3L, 4L, "1A", PaymentMethod.CASH
        );
        TicketCreateRequest request2 = new TicketCreateRequest(
                5L, 2L, 3L, 4L, "2B", PaymentMethod.CARD
        );

        Ticket ticket1 = ticketMapper.toEntity(request1);
        Ticket ticket2 = ticketMapper.toEntity(request2);

        assertTrue(ticket1.getQrCode().contains("TICKET-1-1A-"));
        assertTrue(ticket2.getQrCode().contains("TICKET-5-2B-"));
        assertNotEquals(ticket1.getQrCode(), ticket2.getQrCode());
    }

    @Test
    @DisplayName("Debe manejar diferentes números de asiento en el QR")
    void shouldHandleDifferentSeatNumbersInQr() {
        String[] seatNumbers = {"1A", "10B", "VIP-5", "PREF_1"};

        for (String seatNumber : seatNumbers) {
            TicketCreateRequest request = new TicketCreateRequest(
                    1L, 2L, 3L, 4L, seatNumber, PaymentMethod.CASH
            );

            Ticket ticket = ticketMapper.toEntity(request);
            assertTrue(ticket.getQrCode().contains(seatNumber));
        }
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {
        TicketCreateRequest request = new TicketCreateRequest(
                100L, 200L, 300L, 400L, "VIP-1", PaymentMethod.QR
        );

        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setId(999L);
        ticket.setPrice(new BigDecimal("150000"));
        ticket.setCreatedAt(LocalDateTime.of(2025, 12, 25, 10, 30));
        ticket.setTrip(Trip.builder()
                .id(100L)
                .date(LocalDate.of(2025, 12, 25))
                .departureAt(LocalDateTime.of(2025, 12, 25, 14, 0))
                .route(Route.builder().id(1L).name("Test Route").build())
                .build());
        ticket.setPassenger(User.builder()
                .id(200L)
                .username("Premium User")
                .build());
        ticket.setFromStop(Stop.builder()
                .id(300L)
                .name("Start Point")
                .build());
        ticket.setToStop(Stop.builder()
                .id(400L)
                .name("End Point")
                .build());

        TicketResponse response = ticketMapper.toResponse(ticket);

        assertEquals(999L, response.id());
        assertEquals(100L, response.tripId());
        assertEquals("2025-12-25", response.tripDate());
        assertEquals("14:00", response.tripTime());
        assertEquals(200L, response.passengerId());
        assertEquals("Premium User", response.passengerName());
        assertEquals(300L, response.fromStopId());
        assertEquals("Start Point", response.fromStopName());
        assertEquals(400L, response.toStopId());
        assertEquals("End Point", response.toStopName());
        assertEquals("VIP-1", response.seatNumber());
        assertEquals(new BigDecimal("150000"), response.price());
        assertEquals("QR", response.paymentMethod());
        assertEquals("SOLD", response.status());
    }
}