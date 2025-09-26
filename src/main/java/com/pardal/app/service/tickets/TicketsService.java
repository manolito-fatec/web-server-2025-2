package com.pardal.app.service.tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.DashboardFilterDto;
import com.pardal.app.entity.dto.TicketCountDto;
import com.pardal.app.entity.dto.TicketsByProductsCountDto;
import com.pardal.app.enums.GroupingPeriods;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketsService {
    long getTicketsCount(DashboardFilterDto filters);

    List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct(Specification<Tickets> baseSpec);

    double getSlaCompliantPercentage(Specification<Tickets> baseSpec);
  
    Double getAverageTicketClosureTimeInHours(Specification<Tickets> baseSpec);

    List<TicketCountDto> getTicketCountByPeriod(DashboardFilterDto filters);

    Long getAllTicketsCount(Specification<Tickets> baseSpec);
}
