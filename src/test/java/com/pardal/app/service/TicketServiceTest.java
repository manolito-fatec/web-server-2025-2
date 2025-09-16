package com.pardal.app.service;

import com.pardal.app.entity.Dto.TicketsByProductsCountDto;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.service.Tickets.TicketsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

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
}
