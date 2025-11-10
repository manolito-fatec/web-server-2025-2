package com.pardal.app.service.insights;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.documents.Forecaster;
import com.pardal.app.entity.documents.TicketInsight;
import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;
import com.pardal.app.entity.dto.insights.SlaPredictionResponseDto;
import com.pardal.app.repository.forecaster.ForecasterRespository;
import com.pardal.app.repository.insight.InsightRepository;
import com.pardal.app.repository.slaPrediction.SlaPredictionRepository;

import com.pardal.app.service.tickets.TicketsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.pardal.app.repository.specification.tickets.util.TicketsUtil.buildTicketSpecificationFromFilterList;;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightsServiceImpl implements InsightsService {

    private final TicketsService ticketsService;
    private final InsightRepository insightRepository;
    private final ForecasterRespository forecasterRepository;
    private final SlaPredictionRepository slaPredictionRepository;

    @Override
    public InsightsDataDto getAllInsightsData(InsightsFilterDto pFilters) {
        filterValidator(pFilters);
        Specification<Tickets> baseSpec = buildTicketSpecificationFromFilterList(pFilters);

        InsightsDataDto response = new InsightsDataDto();

        response.setParetoInsightData(ticketsService.getCountSubcategory(baseSpec));
        response.setProductInsightsData(findLatestByCompanyId(pFilters));
        response.setSeasonalityInsightData(findForecasters(pFilters));
        response.setSlaInsightData(getTopSlaRiskBySubcategory(pFilters));
        return response;
    }

    /**
     * Retrieves the latest ticket insights based on a list of customer and product IDs.
     * @param pFilters A DTO containing the lists of customer IDs and product IDs for filtering.
     * @author otavio
     * @return A {@code List<TicketInsight>} of the latest insights matching the criteria,
     * or an empty list if an error occurs during fetching.
     */
    private List<TicketInsight> findLatestByCompanyId(InsightsFilterDto pFilters)
    {
        try {
            return insightRepository.findLatestInsightsByCompanyIdOrProductId(pFilters.getCustomerIds(), pFilters.getProductIds());
        } catch (Exception e) {
            log.error("Erro ao buscar informações do Insights" + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves Forecaster records based on the provided companyId.
     *
     * @param companyId the ID of the company to filter by (can be null)
     * @author paulo arantes
     * @return a list of Forecaster records matching the filter
     */
    private List<Forecaster> findForecasters(InsightsFilterDto pFilters)
    {
        try{
            return forecasterRepository.findByCompanyIdOrProductId(pFilters.getCustomerIds(), pFilters.getProductIds());
        }catch (Exception e) {
            log.error("Erro ao buscar informações do forecaster: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves the top 3 subcategories with the highest average SLA breach probability,
     * optionally filtered by a list of customer IDs and product IDs.
     *
     * @param pFilters The {@code InsightsFilterDto} containing the optional lists of 
     * customer IDs and product IDs for filtering.
     * @author andré
     * @return A {@code List} of the top 3 {@code SlaPredictionResponseDto} objects, 
     * or an empty list if an error occurs during data retrieval.
     */
    private List<SlaPredictionResponseDto> getTopSlaRiskBySubcategory(InsightsFilterDto pFilters) {
        try {
            return slaPredictionRepository.findTop3ByCompanyIdGroupedBySubcategory(pFilters.getCustomerIds(), pFilters.getProductIds());
        } catch (Exception e) {
            log.error("Erro ao buscar informações sobre SLA: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Replaces {@code null} customer and product ID lists in the filter DTO with 
     * an empty, immutable list to prevent {@code NullPointerException}s.
     * @param pFilters The filter DTO to validate and modify.
     * @author paulo arantes
     */
    private void filterValidator(InsightsFilterDto pFilters)
    {
        pFilters.setCustomerIds(
                Optional.ofNullable(pFilters.getCustomerIds()).orElseGet(List::of)
                );

        pFilters.setProductIds(
                Optional.ofNullable(pFilters.getProductIds()).orElseGet(List::of)
                );
    }
}
