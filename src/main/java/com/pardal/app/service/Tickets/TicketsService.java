package com.pardal.app.service.Tickets;

import com.pardal.app.entity.Dto.TicketsByProductsCountDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for retrieving ticket-related data.
 * <p>
 * This interface defines methods for querying ticket counts, either as a total count
 * based on various filters or as a count grouped by product.
 * </p>
 */
public interface TicketsService {

    /**
     * Retrieves the total count of tickets based on provided filters.
     * The method calculates the total number of tickets that match the optional criteria,
     * including product ID, client ID, and a date range.
     *
     * @param productId Optional ID of the product to filter by.
     * @param clientId Optional ID of the client to filter by.
     * @param dateMin Optional start date of the time range to filter by.
     * @param dateMax Optional end date of the time range to filter by.
     * @return The total number of tickets matching the specified filters.
     */
    long getTicketsCount(Optional<Integer> productId, Optional<Integer> clientId, Optional<LocalDateTime> dateMin, Optional<LocalDateTime> dateMax);
    /**
     * Retrieves the count of tickets grouped by product.
     * This method fetches the total number of tickets associated with each product
     * and returns the data as a list of {@link TicketsByProductsCountDto}.
     * @return A list of DTOs, where each DTO contains a product ID and its corresponding ticket count.
     * @see TicketsByProductsCountDto
     */
    List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct();
}
