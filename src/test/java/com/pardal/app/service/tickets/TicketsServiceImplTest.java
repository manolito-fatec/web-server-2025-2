package com.pardal.app.service.tickets;

import com.pardal.app.repository.TicketRepository;
import com.pardal.app.service.tickets.TicketsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketsServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketsServiceImpl ticketsService;

    @Test
    @DisplayName("Should return count when all filters are provided")
    void getTicketsCount_whenAllFiltersAreProvided_shouldReturnCount() {
        long expectedCount = 15L;
        Integer productId = 1;
        Integer clientId = 100;
        LocalDateTime dateMin = LocalDateTime.now().minusDays(10);
        LocalDateTime dateMax = LocalDateTime.now();

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(
                Optional.of(productId),
                Optional.of(clientId),
                Optional.of(dateMin),
                Optional.of(dateMax)
        );

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("Should return total count when no filters are provided")
    void getTicketsCount_whenNoFiltersAreProvided_shouldReturnCount() {
        long expectedCount = 250L;
        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("Should return count when a subset of filters is provided")
    void getTicketsCount_whenSomeFiltersAreProvided_shouldReturnCount() {
        long expectedCount = 42L;
        Integer clientId = 123;
        LocalDateTime dateMin = LocalDateTime.parse("2025-09-01T00:00:00");

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(
                Optional.empty(),
                Optional.of(clientId),
                Optional.of(dateMin),
                Optional.empty()
        );

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("Should return zero when repository finds no matching tickets")
    void getTicketsCount_whenRepositoryReturnsZero_shouldReturnZero() {
        long expectedCount = 0L;
        Integer productId = 999;

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(
                Optional.of(productId),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }
}