package com.pardal.app.service.Tickets;


import java.time.LocalDateTime;
import java.util.Optional;

public interface TicketsService {
    long getTicketsCount(Optional<Integer> productId, Optional<Integer> clientId, Optional<LocalDateTime> dateMin, Optional<LocalDateTime> dateMax);
}
