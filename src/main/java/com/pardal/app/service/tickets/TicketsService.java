package com.pardal.app.service.tickets;

import com.pardal.app.entity.dto.TicketsByProductsCountDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketsService {
    long getTicketsCount(Optional<Integer> productId,
                         Optional<Integer> clientId,
                         Optional<LocalDateTime> dateMin,
                         Optional<LocalDateTime> dateMax);
    List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct();
}
