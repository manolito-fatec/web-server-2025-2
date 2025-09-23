package com.pardal.app.service.tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.TicketsByProductsCountDto;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketsService {
    long getTicketsCount(Optional<Integer> productId,
                         Optional<Integer> clientId,
                         Optional<LocalDateTime> dateMin,
                         Optional<LocalDateTime> dateMax);

    List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct();

    double getSlaCompliantPercentage(Specification<Tickets> baseSpec);
  
    Double getAverageTicketClosureTimeInHours(Specification<Tickets> baseSpec);
}
