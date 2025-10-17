package com.pardal.app.service.tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.metrics.DashboardFilterDto;
import com.pardal.app.entity.dto.metrics.TicketCountDto;
import com.pardal.app.entity.dto.metrics.TicketsByProductsCountDto;
import com.pardal.app.entity.dto.metrics.TicketsBySubcategoryCountDto;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public interface TicketsService {
    long getTicketsCount(DashboardFilterDto filters);

    List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct(Specification<Tickets> baseSpec);

    double getSlaCompliantPercentage(Specification<Tickets> baseSpec);
  
    Double getAverageTicketClosureTimeInHours(Specification<Tickets> baseSpec);

    List<TicketCountDto> getTicketCountByPeriod(DashboardFilterDto filters);

    Long getAllTicketsCount(Specification<Tickets> baseSpec);

    BigDecimal getReopenedTicket(DashboardFilterDto pFilters);

    List<TicketsBySubcategoryCountDto> getCountSubcategory(Specification<Tickets> baseSpec);
}
