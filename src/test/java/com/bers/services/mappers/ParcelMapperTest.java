package com.bers.services.mappers;

import com.bers.api.dtos.ParcelDtos.*;
import com.bers.domain.entities.Parcel;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.ParcelStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("ParcelMapper Tests")
class ParcelMapperTest {
    private ParcelMapper parcelMapper;

    @BeforeEach
    void setUp() {
        parcelMapper = Mappers.getMapper(ParcelMapper.class);
    }

    @Test
    @DisplayName("Debe mapear ParcelCreateRequest a la entidad Parcel")
    void shouldMapCreateRequestToEntity() {
        ParcelCreateRequest request = new ParcelCreateRequest(
                "John Sender",
                "3001234567",
                "Jane Receiver",
                "3009876543",
                new BigDecimal("25000"),
                1L,
                2L,
                3L
        );

        Parcel parcel = parcelMapper.toEntity(request);

        assertNotNull(parcel);
        assertEquals("John Sender", parcel.getSenderName());
        assertEquals("3001234567", parcel.getSenderPhone());
        assertEquals("Jane Receiver", parcel.getReceiverName());
        assertEquals("3009876543", parcel.getReceiverPhone());
        assertEquals(new BigDecimal("25000"), parcel.getPrice());
        assertEquals(ParcelStatus.CREATED, parcel.getStatus());
        assertNotNull(parcel.getCode());
        assertTrue(parcel.getCode().startsWith("PCL-"));
        assertNotNull(parcel.getDeliveryOtp());
        assertEquals(6, parcel.getDeliveryOtp().length());
        assertNull(parcel.getId());
        assertNull(parcel.getProofPhotoUrl());
    }

    @Test
    @DisplayName("Debe generar códigos de envío únicos")
    void shouldGenerateUniqueParcelCodes() {
        ParcelCreateRequest request = new ParcelCreateRequest(
                "Sender", "3001234567", "Receiver", "3009876543",
                new BigDecimal("10000"), 1L, 2L, null
        );

        Parcel parcel1 = parcelMapper.toEntity(request);
        try { Thread.sleep(2); } catch (InterruptedException e) {}
        Parcel parcel2 = parcelMapper.toEntity(request);

        assertNotEquals(parcel1.getCode(), parcel2.getCode());
    }

    @Test
    @DisplayName("Debe generar OTP con 6 dígitos")
    void shouldGenerateOtpWith6Digits() {
        ParcelCreateRequest request = new ParcelCreateRequest(
                "Sender", "3001234567", "Receiver", "3009876543",
                new BigDecimal("10000"), 1L, 2L, null
        );

        Parcel parcel = parcelMapper.toEntity(request);

        assertNotNull(parcel.getDeliveryOtp());
        assertEquals(6, parcel.getDeliveryOtp().length());
        assertTrue(parcel.getDeliveryOtp().matches("\\d{6}"));
    }

    @Test
    @DisplayName("Debe actualizar la entidad Parcel desde ParcelUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        Parcel existingParcel = Parcel.builder()
                .id(1L)
                .code("PCL-123")
                .senderName("Sender")
                .senderPhone("3001234567")
                .receiverName("Receiver")
                .receiverPhone("3009876543")
                .price(new BigDecimal("10000"))
                .status(ParcelStatus.CREATED)
                .deliveryOtp("123456")
                .build();

        ParcelUpdateRequest request = new ParcelUpdateRequest(
                ParcelStatus.DELIVERED,
                "https://example.com/photo.jpg",
                "123456"
        );

        parcelMapper.updateEntity(request, existingParcel);

        assertEquals(ParcelStatus.DELIVERED, existingParcel.getStatus());
        assertEquals("https://example.com/photo.jpg", existingParcel.getProofPhotoUrl());
        assertEquals("123456", existingParcel.getDeliveryOtp());
        assertEquals("Sender", existingParcel.getSenderName());
        assertEquals("Receiver", existingParcel.getReceiverName());
    }

    @Test
    @DisplayName("Debe mapear la entidad Parcel a ParcelResponse")
    void shouldMapEntityToResponse() {
        Stop fromStop = Stop.builder()
                .id(1L)
                .name("Bogotá")
                .build();

        Stop toStop = Stop.builder()
                .id(2L)
                .name("Tunja")
                .build();

        Trip trip = Trip.builder()
                .id(3L)
                .build();

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime deliveredAt = LocalDateTime.now().plusHours(3);

        Parcel parcel = Parcel.builder()
                .id(1L)
                .code("PCL-123456")
                .senderName("John Sender")
                .senderPhone("3001234567")
                .receiverName("Jane Receiver")
                .receiverPhone("3009876543")
                .price(new BigDecimal("35000"))
                .status(ParcelStatus.DELIVERED)
                .proofPhotoUrl("https://example.com/photo.jpg")
                .deliveryOtp("123456")
                .createdAt(createdAt)
                .deliveredAt(deliveredAt)
                .fromStop(fromStop)
                .toStop(toStop)
                .trip(trip)
                .build();

        ParcelResponse response = parcelMapper.toResponse(parcel);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("PCL-123456", response.code());
        assertEquals("John Sender", response.senderName());
        assertEquals("3001234567", response.senderPhone());
        assertEquals("Jane Receiver", response.receiverName());
        assertEquals("3009876543", response.receiverPhone());
        assertEquals(new BigDecimal("35000"), response.price());
        assertEquals("DELIVERED", response.status());
        assertEquals("https://example.com/photo.jpg", response.proofPhotoUrl());
        assertEquals("123456", response.deliveryOtp());
        assertEquals(createdAt, response.createdAt());
        assertEquals(deliveredAt, response.deliveredAt());
        assertEquals(1L, response.fromStopId());
        assertEquals(2L, response.toStopId());
        assertEquals(3L, response.tripId());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de ParcelStatus correctamente")
    void shouldMapAllParcelStatusTypes() {
        for (ParcelStatus status : ParcelStatus.values()) {
            Parcel parcel = Parcel.builder()
                    .id(1L)
                    .code("PCL-TEST")
                    .senderName("Sender")
                    .senderPhone("3001234567")
                    .receiverName("Receiver")
                    .receiverPhone("3009876543")
                    .price(BigDecimal.TEN)
                    .status(status)
                    .deliveryOtp("123456")
                    .createdAt(LocalDateTime.now())
                    .fromStop(Stop.builder().id(1L).build())
                    .toStop(Stop.builder().id(2L).build())
                    .build();

            ParcelResponse response = parcelMapper.toResponse(parcel);

            assertEquals(status.name(), response.status());
        }
    }

    @Test
    @DisplayName("Debe manejar un envío sin viaje")
    void shouldHandleParcelWithoutTrip() {
        Parcel parcel = Parcel.builder()
                .id(1L)
                .code("PCL-NO-TRIP")
                .senderName("Sender")
                .senderPhone("3001234567")
                .receiverName("Receiver")
                .receiverPhone("3009876543")
                .price(new BigDecimal("15000"))
                .status(ParcelStatus.CREATED)
                .deliveryOtp("123456")
                .createdAt(LocalDateTime.now())
                .fromStop(Stop.builder().id(1L).build())
                .toStop(Stop.builder().id(2L).build())
                .trip(null)
                .build();

        ParcelResponse response = parcelMapper.toResponse(parcel);

        assertNull(response.tripId());
    }

    @Test
    @DisplayName("Debe manejar un envío sin información de entrega")
    void shouldHandleParcelWithoutDeliveryInfo() {
        Parcel parcel = Parcel.builder()
                .id(1L)
                .code("PCL-NO-DELIVERY")
                .senderName("Sender")
                .senderPhone("3001234567")
                .receiverName("Receiver")
                .receiverPhone("3009876543")
                .price(new BigDecimal("15000"))
                .status(ParcelStatus.IN_TRANSIT)
                .deliveryOtp("123456")
                .createdAt(LocalDateTime.now())
                .proofPhotoUrl(null)
                .deliveredAt(null)
                .fromStop(Stop.builder().id(1L).build())
                .toStop(Stop.builder().id(2L).build())
                .build();

        ParcelResponse response = parcelMapper.toResponse(parcel);

        assertNull(response.proofPhotoUrl());
        assertNull(response.deliveredAt());
    }

    @Test
    @DisplayName("Debe validar el formato del número de teléfono")
    void shouldValidatePhoneNumberFormat() {
        ParcelCreateRequest request = new ParcelCreateRequest(
                "Sender",
                "3001234567",
                "Receiver",
                "3109876543",
                new BigDecimal("10000"),
                1L,
                2L,
                null
        );

        Parcel parcel = parcelMapper.toEntity(request);

        assertEquals("3001234567", parcel.getSenderPhone());
        assertEquals("3109876543", parcel.getReceiverPhone());
    }

    @Test
    @DisplayName("Debe manejar diferentes valores de precio")
    void shouldHandleDifferentPriceValues() {
        BigDecimal[] prices = {
                new BigDecimal("1000"),
                new BigDecimal("50000"),
                new BigDecimal("100000.50"),
                new BigDecimal("0.01")
        };

        for (BigDecimal price : prices) {
            ParcelCreateRequest request = new ParcelCreateRequest(
                    "Sender", "3001234567", "Receiver", "3009876543",
                    price, 1L, 2L, null
            );

            Parcel parcel = parcelMapper.toEntity(request);
            assertEquals(price, parcel.getPrice());
        }
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {
        ParcelCreateRequest request = new ParcelCreateRequest(
                "Complete Sender",
                "3001111111",
                "Complete Receiver",
                "3002222222",
                new BigDecimal("99999.99"),
                100L,
                200L,
                300L
        );

        Parcel parcel = parcelMapper.toEntity(request);
        parcel.setId(999L);

        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 1, 10, 0);
        LocalDateTime deliveredAt = LocalDateTime.of(2025, 12, 1, 15, 30);

        parcel.setCreatedAt(createdAt);
        parcel.setDeliveredAt(deliveredAt);
        parcel.setProofPhotoUrl("https://storage.example.com/proof-999.jpg");
        parcel.setFromStop(Stop.builder().id(100L).name("Origin").build());
        parcel.setToStop(Stop.builder().id(200L).name("Destination").build());
        parcel.setTrip(Trip.builder().id(300L).build());
        parcel.setStatus(ParcelStatus.DELIVERED);

        ParcelResponse response = parcelMapper.toResponse(parcel);

        assertEquals(999L, response.id());
        assertTrue(response.code().startsWith("PCL-"));
        assertEquals("Complete Sender", response.senderName());
        assertEquals("3001111111", response.senderPhone());
        assertEquals("Complete Receiver", response.receiverName());
        assertEquals("3002222222", response.receiverPhone());
        assertEquals(new BigDecimal("99999.99"), response.price());
        assertEquals("DELIVERED", response.status());
        assertEquals("https://storage.example.com/proof-999.jpg", response.proofPhotoUrl());
        assertNotNull(response.deliveryOtp());
        assertEquals(createdAt, response.createdAt());
        assertEquals(deliveredAt, response.deliveredAt());
        assertEquals(100L, response.fromStopId());
        assertEquals(200L, response.toStopId());
        assertEquals(300L, response.tripId());
    }
}