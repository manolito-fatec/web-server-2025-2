package com.pardal.app.service.tickets;

import com.pardal.app.entity.dto.TicketsByProductsCountDto;
import com.pardal.app.entity.Tickets;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.repository.specification.MetricsSpecifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TicketsServiceImpl implements TicketsService {

    @Autowired
    private TicketRepository ticketsRepository;

    @Autowired
    private MetricsSpecifications metricsSpecifications;

    /**
     * Calculates the number of tickets based on the provided filters.
     * <p>
     * This method dynamically builds a JPA {@link Specification} based on optional filters
     * for product ID, client ID, and a date range. It then uses this specification to
     * count the total number of tickets that match the criteria.
     * </p>
     *
     * @author caue
     * @param productId the optional product ID to filter by
     * @param clientId the optional client ID to filter by
     * @param dateMin the optional minimum date to filter by
     * @param dateMax the optional maximum date to filter by
     * @return the total number of tickets that meet all specified filter criteria
     */
    @Override
    public long getTicketsCount(Optional<Integer> productId,
                                Optional<Integer> clientId,
                                Optional<LocalDateTime> dateMin,
                                Optional<LocalDateTime> dateMax) {

        Specification<Tickets> spec = Specification.where(null);

        if (productId.isPresent()) {
            spec = spec.and(metricsSpecifications.hasProductId(productId.get()));
        }
        if (clientId.isPresent()) {
            spec = spec.and(metricsSpecifications.hasClientId(clientId.get()));
        }
        if (dateMin.isPresent()) {
            spec = spec.and(metricsSpecifications.hasDateAfter(dateMin.get()));
        }
        if (dateMax.isPresent()) {
            spec = spec.and(metricsSpecifications.hasDateBefore(dateMax.get()));
        }

        return ticketsRepository.count(spec);
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
    public List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct() {
        return ticketsRepository.getTicketsCountGroupedByProduct();
    }

    @Override
    public Double getAverageTicketClosureTimeInHours() {
        List<Tickets> closedTickets = ticketsRepository.findAllByClosedAtIsNotNull();

        if (closedTickets.isEmpty()) {
            throw new NoSuchElementException("No tickets found for calculation");
        }

        long totalDurationInSeconds = closedTickets.stream()
                .mapToLong(ticket -> Duration.between(ticket.getCreatedAt(), ticket.getClosedAt()).getSeconds())
                .sum();

        double averageTime = (double) totalDurationInSeconds / closedTickets.size() / 3600.0;

        return Math.round(averageTime * 100.0) / 100.0;
    }
}
