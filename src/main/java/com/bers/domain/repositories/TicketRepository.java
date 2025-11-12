package com.bers.domain.repositories;

import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.domain.entities.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByQrCode(String qrCode);

    List<Ticket> findByTripIdAndStatus(Long tripId, TicketStatus status);

    List<Ticket> findByPassengerIdAndStatus(Long passengerId, TicketStatus status);

    List<Ticket> findByPassengerId(Long passengerId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.trip tr JOIN FETCH t.passenger " +
            "WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(Long id);

    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId " +
            "AND t.seatNumber = :seatNumber AND t.status = 'SOLD'")
    Optional<Ticket> findSoldTicketBySeat(@Param("tripId") Long tripId,
                                          @Param("seatNumber") String seatNumber);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.trip.id = :tripId " +
            "AND t.status = 'SOLD'")
    long countSoldTicketsByTrip(@Param("tripId") Long tripId);

    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId " +
            "AND t.status IN ('SOLD') " +
            "AND ((t.fromStop.order <= :stopOrder AND t.toStop.order > :stopOrder))")
    List<Ticket> findActiveTicketsForStop(@Param("tripId") Long tripId,
                                          @Param("stopOrder") Integer stopOrder);

    @Query("SELECT t FROM Ticket t WHERE t.trip.departureAt < :dateTime " +
            "AND t.status = 'SOLD'")
    List<Ticket> findTicketsForDepartedTrips(@Param("dateTime") LocalDateTime dateTime);

    @Query("""
    SELECT t\s
    FROM Ticket t
    WHERE t.trip.id = :tripId
      AND t.seatNumber = :seatNumber
      AND t.status IN ('SOLD', 'RESERVED')
""")
    List<Ticket> findByTripIdAndSeatNumber(@Param("tripId") Long tripId,
                                           @Param("seatNumber") String seatNumber);

    List<Trip> findByStatusAndTrip_DepartureAtBefore(TripStatus status, LocalDateTime dateTime);

    @Query("SELECT t FROM Trip t WHERE t.status = :status " +
            "AND t.departureAt BETWEEN :start AND :end")
    List<Trip> findByStatusAndDepartureAtBetween(
            @Param("status") TripStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
