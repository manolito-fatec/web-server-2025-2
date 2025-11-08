package com.pardal.app.repository.specification.tickets.util;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;
import com.pardal.app.repository.specification.tickets.TicketsFilters;
import com.pardal.app.repository.specification.tickets.TicketsSpecification;
import org.springframework.data.jpa.domain.Specification;

public final class TicketsUtil {

    /**
     * Creates a {@link Specification} for {@link Tickets} using the given filters.
     *
     * @author Caue
     * @param pFilters filters with date range and other criteria
     * @return a {@link Specification} for filtering {@link Tickets}
     */
    public static Specification<Tickets> buildTicketSpecificationFromFilters(TicketsFilters pFilters) {
        return TicketsSpecification.withDateRangeAndFilters(pFilters);
    }

    /**
     * Creates a {@link Specification} for {@link Tickets} using the given filters.
     *
     * @author paulo
     * @param pFilters filters with date range and other criteria
     * @return a {@link Specification} for filtering {@link Tickets}
     */
   public static Specification<Tickets> buildTicketSpecificationFromFilterList(InsightsFilterDto pFilters) {
       return TicketsSpecification.withDateRangeAndFilterList(pFilters);
   }
}
