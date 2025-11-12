package com.bers.services.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatAvailableEventListener {

    // Aqui puedes inyectar servicios de notificación si los tienes

    @EventListener
    public void handleSeatAvailable(SeatAvailableEvent event) {
        log.info("🎫 SEAT AVAILABLE FOR QUICK SALE!");
        log.info("Trip: {}, Seat: {}, Segment: {} → {}",
                event.getTripId(),
                event.getSeatNumber(),
                event.getFromStopId(),
                event.getToStopId()
        );

        // Aqui puedes:
        // 1. Enviar notificacion push a apps móviles
        // 2. Actualizar dashboard en tiempo real (WebSocket)
        // 3. Enviar SMS/WhatsApp a clerks en terminal
        // 4. Actualizar pantallas de informacion en terminal
    }
}
