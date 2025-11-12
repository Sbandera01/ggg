package com.bers.services.service;

import com.bers.api.dtos.QuickSaleDtos.*;

public interface QuickSaleService {
    QuickSaleResponse createQuickSale(QuickSaleRequest request);
    AvailableQuickSaleSeatsResponse getAvailableQuickSaleSeats(Long tripId);
}
