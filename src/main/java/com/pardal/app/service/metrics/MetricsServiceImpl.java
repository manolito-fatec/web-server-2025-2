package com.pardal.app.service.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Product;
import com.pardal.app.entity.TicketStatusHistory;
import com.pardal.app.entity.dto.ChartDto;
import com.pardal.app.entity.dto.FilterDataDto;
import com.pardal.app.repository.CompanyRepository;
import com.pardal.app.repository.ProductRepository;
import com.pardal.app.repository.TicketStatusHistoryRepository;
import com.pardal.app.repository.specification.MetricsSpecifications;
import com.pardal.app.service.tickets.TicketsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService
{
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final TicketsService ticketsService;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final MetricsSpecifications metricsSpecifications;

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
     * @param pProductId  the ID of the product to filter by (nullable, optional filter)
     * @param pCustomerId the ID of the customer to filter by (nullable, optional filter)
     * @param pFromDate   the start date of the time range for filtering data (nullable, optional)
     * @param pToDate     the end date of the time range for filtering data (nullable, optional)
     * @return a {@link ChartDto} containing all calculated values and metrics for charts and cards
     */
    @Override
    public ChartDto getAllChartData ( Optional<Integer> pProductId,
            Optional<Integer> pCustomerId,
            Optional<LocalDateTime> pFromDate,
            Optional<LocalDateTime> pToDate )
    {
        ChartDto response = new ChartDto();
        response.setRecidivismRate(getReopenedTicket(pProductId,pCustomerId,pFromDate,pToDate));
        response.setTicketsCountGroupedByProduct(ticketsService.getTicketsCountGroupedByProduct());
        response.setTicketClosureTimeInHours(ticketsService.getAverageTicketClosureTimeInHours());
        return response;
    }

    /**
     * Calculates the recidivism rate of tickets (percentage of reopened tickets) based on the provided filters.
     * <p>
     * This method retrieves the total number of tickets and the number of reopened tickets within the specified
     * product, customer, and date range filters. It then calculates the ratio of reopened tickets to total tickets.
     * </p>
     *
     * @author paulo
     * @param pProductId  the product ID used to filter tickets (nullable, optional)
     * @param pCustomerId the customer ID used to filter tickets (nullable, optional)
     * @param pFromDate   the start date of the period to filter by (nullable, optional)
     * @param pToDate     the end date of the period to filter by (nullable, optional)
     * @return the recidivism rate as a {@code Double}, representing the proportion of reopened tickets
     *         relative to the total number of tickets; returns {@code 0.0} if there are no tickets
     */
    protected BigDecimal getReopenedTicket(Optional<Integer> pProductId,
            Optional<Integer> pCustomerId,
            Optional<LocalDateTime> pFromDate,
            Optional<LocalDateTime> pToDate)
    {
        long totalOfTickets = ticketsService.getTicketsCount(pProductId, pCustomerId, pFromDate, pToDate);

        Specification<TicketStatusHistory> reopenedSpec =
                Specification.where(metricsSpecifications.isReOpened())
                             .and(metricsSpecifications.joinWithTicket(
                                     pProductId, pCustomerId, pFromDate, pToDate));

        long totalOfTicketsReopened = ticketStatusHistoryRepository.count(reopenedSpec);

        BigDecimal reopened = BigDecimal.valueOf(totalOfTicketsReopened);
        BigDecimal total = (totalOfTickets == 0)
                 ? BigDecimal.ZERO 
                 : BigDecimal.valueOf(totalOfTickets);

        return reopened
                .divide(total, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

}
