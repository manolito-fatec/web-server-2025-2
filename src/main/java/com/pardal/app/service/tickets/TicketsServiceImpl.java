package com.pardal.app.service.tickets;

import com.pardal.app.entity.dto.DashboardFilterDto;
import com.pardal.app.entity.dto.TicketCountDto;
import com.pardal.app.entity.dto.TicketsByProductsCountDto;
import com.pardal.app.entity.TicketStatusHistory;
import com.pardal.app.entity.Tickets;
import com.pardal.app.enums.GroupingPeriods;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.repository.TicketStatusHistoryRepository;
import com.pardal.app.repository.specification.TicketsSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketsServiceImpl implements TicketsService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketStatusHistoryRepository ticketStatusHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Calculates the number of tickets based on the provided filters.
     * <p>
     * This method dynamically builds a JPA {@link Specification} based on optional filters
     * for product ID, client ID, and a date range. It then uses this specification to
     * count the total number of tickets that match the criteria.
     * </p>
     *
     * @author caue
     * @param pFilters  the DTO with information of the product, the customer,
     *                  the start date of the time range,
     *                  the end date of the time range
     *                  and the period to filter by (all of them are nullable, optional filters);
     * @return the total number of tickets that meet all specified filter criteria
     */
    @Override
    public long getTicketsCount(DashboardFilterDto pFilters) {

        Specification<Tickets> spec = buildSpecificationFromFilters(pFilters);

        return ticketRepository.count(spec);
    }

    /**
     * Retrieves the count of tickets grouped by product.
     * <p>
     * This method executes a query to count all tickets and groups the results by product,
     * returning a list of {@link TicketsByProductsCountDto} objects. Each object contains
     * the product information and the corresponding ticket count.
     * </p>
     * @author gabriel
     *
     * @return a {@code List} of {@link TicketsByProductsCountDto} objects, each representing a product
     * and the total number of tickets associated with it
     */
    @Override
    public List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct(Specification<Tickets> baseSpec) {
        Specification<Tickets> productCountSpec = baseSpec.and(TicketsSpecification.findTicketsByProduct());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Tickets> root = query.from(Tickets.class);

        Predicate predicate = productCountSpec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        List<Tuple> tuples =entityManager.createQuery(query).getResultList();

        return tuples.stream()
                .map(p -> new TicketsByProductsCountDto(
                        p.get("productId", Integer.class),
                        p.get("productName", String.class),
                        p.get("totalTickets", Long.class)))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the SLA compliant tickets percentual.
     *
     * @param baseSpec A specification with the compliance filters.
     * @return A DTO with the calculated SLA compliant tickets percentual.
     */
    public double getSlaCompliantPercentage(Specification<Tickets> baseSpec) {
        long totalTickets = ticketRepository.count(baseSpec);

        if (totalTickets == 0) {
            return 0.0;
        }

        Specification<Tickets> slaCompliantSpec = baseSpec.and(TicketsSpecification.isSlaMet());
        long slaCompliantTickets = ticketRepository.count(slaCompliantSpec);

        return ((double) slaCompliantTickets / totalTickets) * 100.0;
    }

    @Override
    public Double getAverageTicketClosureTimeInHours(Specification<Tickets> baseSpec) {
        Specification<Tickets> finalSpec = Specification.where(baseSpec).and(TicketsSpecification.isClosed());

        List<Tickets> closedTickets = ticketRepository.findAll(finalSpec);

        if (closedTickets.isEmpty()) {
            return 0.0;
        }

        long totalDurationInSeconds = closedTickets.stream()
                .mapToLong(ticket -> Duration.between(ticket.getCreatedAt(), ticket.getClosedAt()).getSeconds())
                .sum();

        return (double) totalDurationInSeconds / closedTickets.size() / 3600.0;
    }

    private Specification<Tickets> buildSpecificationFromFilters(DashboardFilterDto pFilters) {
        return TicketsSpecification.withDateRangeAndFilters(pFilters);
    }

    /**
     * Gets the number of tickets grouped by period based on the given filters.
     * @author Andre
     * @param filters filters with date range and period settings
     * @return a list of {@link TicketCountDto} with ticket counts by period
     */
    public List<TicketCountDto> getTicketCountByPeriod(DashboardFilterDto filters) {

        Specification<Tickets> filterSpec = TicketsSpecification.withDateRangeAndFilters(filters);

        List<Tickets> filteredTickets = ticketRepository.findAll(filterSpec);

        DateTimeFormatter formatter = getFormatterForPeriod(filters.getPeriods());

        return filteredTickets.stream()
                .collect(Collectors.groupingBy(
                        ticket -> formatDate(ticket.getCreatedAt(), formatter),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> new TicketCountDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TicketCountDto::getPeriod))
                .collect(Collectors.toList());
    }

    private DateTimeFormatter getFormatterForPeriod(GroupingPeriods period) {
        return switch (period) {
            case MONTH -> DateTimeFormatter.ofPattern("yyyy-MM");
            case YEAR -> DateTimeFormatter.ofPattern("yyyy");
            default -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
        };
    }

    private String formatDate(java.time.Instant date, DateTimeFormatter formatter) {
        return formatter.format(java.time.LocalDateTime.ofInstant(date, java.time.ZoneId.systemDefault()));
    }

    /**
     * Returns the total number of {@link Tickets} matching the given specification.
     * @author Caue
     * @param baseSpec the {@link Specification} used to filter tickets
     * @return the total count of matching {@link Tickets}
     */
    public Long getAllTicketsCount(Specification<Tickets> baseSpec) {
        return ticketRepository.count(baseSpec);
    }

    /**
     * Calculates the recidivism rate of tickets (percentage of reopened tickets) based on the provided filters.
     * <p>
     * This method retrieves the total number of tickets and the number of reopened tickets within the specified
     * product, customer, and date range filters. It then calculates the ratio of reopened tickets to total tickets.
     * </p>
     *
     * @author paulo
     * @param pFilters  the DTO with information of the product, the customer,
     *                  the start date of the time range,
     *                  the end date of the time range
     *                  and the period to filter by (all of them are nullable, optional filters);
     * @return the recidivism rate as a {@code Double}, representing the proportion of reopened tickets
     *         relative to the total number of tickets; returns {@code 0.0} if there are no tickets
     */
    public BigDecimal getReopenedTicket(DashboardFilterDto pFilters)
    {
        long totalOfTickets = getTicketsCount(pFilters);

        if(totalOfTickets == 0)
        {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.valueOf(totalOfTickets);

        Specification<TicketStatusHistory> reopenedSpec =
                Specification.where(TicketsSpecification.isReOpened())
                             .and(TicketsSpecification.joinWithTicketStatusHistory(
                                     Optional.ofNullable(pFilters.getProductId()),
                                     Optional.ofNullable(pFilters.getCustomerId()),
                                     Optional.ofNullable(pFilters.getFromDate()),
                                     Optional.ofNullable(pFilters.getToDate())));

        long totalOfTicketsReopened = ticketStatusHistoryRepository.count(reopenedSpec);

        BigDecimal reopened = BigDecimal.valueOf(totalOfTicketsReopened);

        return reopened
                .divide(total, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
