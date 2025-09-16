package com.pardal.app.service;

import com.pardal.app.entity.Dto.TicketsByProductsCountDto;
import com.pardal.app.entity.Tickets;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.service.Tickets.TicketsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketsRepository;

    @InjectMocks
    private TicketsServiceImpl ticketService;

    @Test
    @DisplayName("getTicketsCountGroupedByProduct - should return ticket counts grouped by product when products exist")
    void getTicketsCountGroupedByProduct_whenProductsExist_shouldReturnCounts() {
        List<TicketsByProductsCountDto> expectedResults = List.of(
                new TicketsByProductsCountDto(1, 10L),
                new TicketsByProductsCountDto(2, 5L),
                new TicketsByProductsCountDto(3, 20L)
        );

        when(ticketsRepository.getTicketsCountGroupedByProduct())
                .thenReturn(expectedResults);

        List<TicketsByProductsCountDto> actualResults = ticketService.getTicketsCountGroupedByProduct();

        assertNotNull(actualResults);
        assertEquals(3, actualResults.size());
        assertEquals(1, actualResults.get(0).getProductId());
        assertEquals(2, actualResults.get(1).getProductId());
        assertEquals(3, actualResults.get(2).getProductId());

        verify(ticketsRepository).getTicketsCountGroupedByProduct();
    }

    @Test
    @DisplayName("getTicketsCountGroupedByProduct - should return empty list when no tickets exist")
    void getTicketsCountGroupedByProduct_whenNoTickets_shouldReturnEmptyList() {
        when(ticketsRepository.getTicketsCountGroupedByProduct())
                .thenReturn(Collections.emptyList());

        List<TicketsByProductsCountDto> actualResults = ticketService.getTicketsCountGroupedByProduct();

        assertNotNull(actualResults);
        assertTrue(actualResults.isEmpty());

        verify(ticketsRepository).getTicketsCountGroupedByProduct();
    }

    @Test
    @DisplayName("getAverageTicketClosureTimeInHours - should calculate and return the average closure time when tickets exist")
    void getAverageTicketClosureTimeInHours_whenTicketsExist_shouldReturnAverageTime() {
        Tickets ticket1 = new Tickets();
        ticket1.setCreatedAt(Instant.parse("2025-09-15T10:00:00Z"));
        ticket1.setClosedAt(Instant.parse("2025-09-15T12:00:00Z"));

        Tickets ticket2 = new Tickets();
        ticket2.setCreatedAt(Instant.parse("2025-09-14T08:00:00Z"));
        ticket2.setClosedAt(Instant.parse("2025-09-14T11:30:00Z"));

        Tickets ticket3 = new Tickets();
        ticket3.setCreatedAt(Instant.parse("2025-09-13T14:00:00Z"));
        ticket3.setClosedAt(Instant.parse("2025-09-13T19:00:00Z"));

        List<Tickets> closedTickets = List.of(ticket1, ticket2, ticket3);

        when(ticketsRepository.findAllByClosedAtIsNotNull())
                .thenReturn(closedTickets);

        Double averageTime = ticketService.getAverageTicketClosureTimeInHours();

        assertEquals(3.5, averageTime);

        verify(ticketsRepository).findAllByClosedAtIsNotNull();
    }

    @Test
    @DisplayName("getAverageTicketClosureTimeInHours - should throw NoSuchElementException when no closed tickets exist")
    void getAverageTicketClosureTimeInHours_whenNoTicketsExist_shouldThrowException() {
        when(ticketsRepository.findAllByClosedAtIsNotNull())
                .thenReturn(Collections.emptyList());

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> ticketService.getAverageTicketClosureTimeInHours()
        );

        assertEquals("No tickets found for calculation", thrown.getMessage());

        verify(ticketsRepository).findAllByClosedAtIsNotNull();
    }
}
