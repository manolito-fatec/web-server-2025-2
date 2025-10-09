package com.pardal.app.service.tickets;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Subcategory;
import com.pardal.app.entity.dto.DashboardFilterDto;
import com.pardal.app.entity.dto.TicketCountDto;
import com.pardal.app.entity.dto.TicketsByProductsCountDto;
import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.TicketsBySubcategoryCountDto;
import com.pardal.app.enums.GroupingPeriods;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.repository.specification.MetricsSpecifications;

import com.pardal.app.repository.specification.TicketsSpecification;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketsServiceImpl implements TicketsService {

    @Autowired
    private TicketRepository ticketRepository;

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
        Specification<Tickets> productCountSpec = baseSpec.and(MetricsSpecifications.findTicketsByProduct());

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

        Specification<Tickets> slaCompliantSpec = baseSpec.and(MetricsSpecifications.isSlaMet());
        long slaCompliantTickets = ticketRepository.count(slaCompliantSpec);

        return ((double) slaCompliantTickets / totalTickets) * 100.0;
    }

    @Override
    public Double getAverageTicketClosureTimeInHours(Specification<Tickets> baseSpec) {
        Specification<Tickets> finalSpec = Specification.where(baseSpec).and(MetricsSpecifications.isClosed());

        List<Tickets> closedTickets = ticketRepository.findAll(finalSpec);

        if (closedTickets.isEmpty()) {
            return 0.0;
        }

        long totalDurationInSeconds = closedTickets.stream()
                .mapToLong(ticket -> Duration.between(ticket.getCreatedAt(), ticket.getClosedAt()).getSeconds())
                .sum();

        return (double) totalDurationInSeconds / closedTickets.size() / 3600.0;
    }

    /**
     * Retrieves the count of tickets grouped by subcategory and company.
     * <p>
     * This method executes a JPA Criteria query to count all tickets and groups the results by
     * **Subcategory** and **Company**, returning a list of {@code TicketsBySubcategoryCountDto} objects.
     * Each object contains the subcategory and company information along with the corresponding ticket count.
     * </p>
     *
     * @author gabriel
     *
     * @param baseSpec a {@code Specification<Tickets>} to apply optional filtering conditions to the query,
     * or {@code null} to count all tickets.
     * @return a {@code List} of {@link TicketsBySubcategoryCountDto} objects, each representing a unique
     * combination of subcategory and company with the total number of tickets associated with it.
     */
    @Override
    public List<TicketsBySubcategoryCountDto> getCountSubcategory(Specification<Tickets> baseSpec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Tickets> root = query.from(Tickets.class);

        Join<Tickets, Subcategory> subcategoryJoin = root.join("subcategory");

        Join<Tickets, Company> companyJoin = root.join("company");

        query.multiselect(
                subcategoryJoin.get("id").alias("subcategoryId"),
                subcategoryJoin.get("name").alias("subcategoryName"),

                companyJoin.get("id").alias("companyId"),
                companyJoin.get("name").alias("companyName"),

                cb.count(root).alias("totalTickets")
        );

        Predicate predicate = baseSpec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        query.groupBy(
                subcategoryJoin.get("id"),
                subcategoryJoin.get("name"),

                companyJoin.get("id"),
                companyJoin.get("name")
        );

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();

        return tuples.stream()
                .map(t -> new TicketsBySubcategoryCountDto(
                        t.get("subcategoryId", Integer.class),
                        t.get("subcategoryName", String.class),
                        t.get("companyId", Integer.class),
                        t.get("companyName", String.class),
                        t.get("totalTickets", Long.class)))
                .collect(Collectors.toList());
    }

    private Specification<Tickets> buildSpecificationFromFilters(DashboardFilterDto pFilters) {
        return TicketsSpecification.withDateRangeAndFilters(pFilters);
    }

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

    public Long getAllTicketsCount(Specification<Tickets> baseSpec) {
        return ticketRepository.count(baseSpec);
    }
}
