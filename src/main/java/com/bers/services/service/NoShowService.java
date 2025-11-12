package com.bers.services.service;

import java.math.BigDecimal;
import java.util.List;

public interface NoShowService {
    void processNoShowTickets(Long tripId);
    void processUpcomingTripsNoShow();
    void releaseNoShowSeat(Long ticketId);
    BigDecimal calculateNoShowFee();
    List<String> getAvailableSeatsFromNoShow(Long tripId);
}