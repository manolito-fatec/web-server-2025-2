package com.pardal.app.service.insights;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.documents.Forecaster;
import com.pardal.app.entity.documents.TicketInsight;
import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;
import com.pardal.app.repository.ForecasterRespository;
import com.pardal.app.repository.InsightRepository;
import com.pardal.app.service.tickets.TicketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

import static com.pardal.app.repository.specification.tickets.util.TicketsUtil.buildTicketSpecificationFromFilters;

@Service
@RequiredArgsConstructor
public class InsightsServiceImpl implements InsightsService {

    private final TicketsService ticketsService;
    private final InsightRepository insightRepository;
    private final ForecasterRespository forecasterRepository;

    @Override
    public InsightsDataDto getAllInsightsData(InsightsFilterDto pFilters) {

        Specification<Tickets> baseSpec = buildTicketSpecificationFromFilters(pFilters);

        InsightsDataDto response = new InsightsDataDto();

        response.setParetoInsightData(ticketsService.getCountSubcategory(baseSpec));
        response.setProductInsightsData(findLatestByCompanyId(pFilters.getCustomerId()));
        response.setSeasonalityInsightData(findForecasters(pFilters.getCustomerId()));
        // response.setAQUI(os dados dos outros cards quando ficarem prontos());

        return response;
    }

    @Override
    public List<TicketInsight> findByCompanyId(Integer companyId) {
        return insightRepository.findByCompanyId(companyId);
    }

    private List<TicketInsight> findLatestByCompanyId(Integer companyId) {
        List<TicketInsight> insights = insightRepository.findLatestInsightsByCompanyId(companyId);

        if (insights.isEmpty()) {
            throw new NoSuchElementException("No insights found for company ID: " + companyId);
        }

        return insights;
    }

    /**
     * Retrieves Forecaster records based on the provided companyId.
     *
     * @param companyId the ID of the company to filter by (can be null)
     * @author paulo arantes
     * @return a list of Forecaster records matching the filter
     */
    private List<Forecaster> findForecasters(Integer companyId)
    {
        return (companyId == null)
        ? forecasterRepository.findAllCompanies()
        : forecasterRepository.findByCompanyId(companyId); 
    }
}
