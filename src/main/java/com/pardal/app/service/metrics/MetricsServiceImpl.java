package com.pardal.app.service.metrics;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.DashboardFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Product;
import com.pardal.app.entity.dto.ChartDto;
import com.pardal.app.entity.dto.FilterDataDto;
import com.pardal.app.repository.CompanyRepository;
import com.pardal.app.repository.ProductRepository;
import com.pardal.app.repository.specification.TicketsSpecification;
import com.pardal.app.service.tickets.TicketsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService
{
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final TicketsService ticketsService;

    /**
     * Retrieves a paginated DTO containing lists of companies and products.
     * <p>
     * This method fetches paginated data for both companies and products based on the
     * provided page number and size, combining them into a {@link FilterDataDto}.
     * </p>
     *
     * @author Cauê
     * @param pPage the page number to retrieve (must be greater than 0)
     * @param pPageSize the number of items per page (must be greater than 0)
     * @return a DTO containing paginated company and product lists
     * @throws IllegalArgumentException if the page number or page size is less than 1
     * @see FilterDataDto
     *
     * @example
     * <pre>{@code
     * // Get the first page with 20 items per page
     * FilterDataDto filterData = filterService.getFilterData(1, 20);
     * }</pre>
     */
    @Override
    public FilterDataDto getFilterData ( int pPage, int pPageSize )
    {
        if (pPage < 1) {
            throw new IllegalArgumentException("O número da página deve ser maior que 0");
        }
        if (pPageSize < 1) {
            throw new IllegalArgumentException("O tamanho da página deve ser maior que 0");
        }

        Pageable pageableRequest = PageRequest.of(pPage - 1, pPageSize);

        Page<Company> companyPage = companyRepository.findAll(pageableRequest);
        Page<Product> productPage = productRepository.findAll(pageableRequest);

        return new FilterDataDto(productPage, companyPage);
    }

    /**
     * Retrieves all chart-related data for the specified filters and returns it as a {@link ChartDto}.
     * <p>
     * This method aggregates and calculates all the necessary information required to populate 
     * dashboard charts and summary cards, such as recidivism rate and other metrics. 
     * The result can be used directly by the front-end to render graphs and KPIs.
     * </p>
     *
     *@author paulo
     * @param pFilters  the DTO with information of the product, the customer,
     *                  the start date of the time range,
     *                  the end date of the time range
     *                  and the period to filter by (all of them are nullable, optional filters);
     * @return a {@link ChartDto} containing all calculated values and metrics for charts and cards
     */
    @Override
    public ChartDto getAllChartData (DashboardFilterDto pFilters)
    {
        Specification<Tickets> baseSpec = buildTicketSpecificationFromFilters(pFilters);

        ChartDto response = new ChartDto();
        response.setRecidivismRate(ticketsService.getReopenedTicket(pFilters));
        response.setTicketsCount(ticketsService.getAllTicketsCount(baseSpec));
        response.setTicketsCountGroupedByProduct(ticketsService.getTicketsCountGroupedByProduct(baseSpec));
        response.setSlaCompliancePercentualDto(ticketsService.getSlaCompliantPercentage(baseSpec));
        response.setTicketClosureTimeInHours(ticketsService.getAverageTicketClosureTimeInHours(baseSpec));
        response.setTicketsCountOverTime(ticketsService.getTicketCountByPeriod(pFilters));
        return response;
    }

    /**
     * Creates a {@link Specification} for {@link Tickets} using the given filters.
     *
     * @author Caue 
     * @param pFilters filters with date range and other criteria
     * @return a {@link Specification} for filtering {@link Tickets}
     */
    private Specification<Tickets> buildTicketSpecificationFromFilters(DashboardFilterDto pFilters) {
        return TicketsSpecification.withDateRangeAndFilters(pFilters);
    }

}
