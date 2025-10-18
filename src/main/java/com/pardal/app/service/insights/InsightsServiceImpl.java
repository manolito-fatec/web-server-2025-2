package com.pardal.app.service.insights;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;
import com.pardal.app.service.tickets.TicketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static com.pardal.app.repository.specification.tickets.util.TicketsUtil.buildTicketSpecificationFromFilters;

@Service
@RequiredArgsConstructor
public class InsightsServiceImpl implements InsightsService {

    private final TicketsService ticketsService;

    @Override
    public InsightsDataDto getAllInsightsData(InsightsFilterDto pFilters) {

        Specification<Tickets> baseSpec = buildTicketSpecificationFromFilters(pFilters);

        InsightsDataDto response = new InsightsDataDto();

        response.setParetoInsightData(ticketsService.getCountSubcategory(baseSpec));
        // response.setAQUI(os dados dos outros cards quando ficarem prontos());

        return response;
    }
}
