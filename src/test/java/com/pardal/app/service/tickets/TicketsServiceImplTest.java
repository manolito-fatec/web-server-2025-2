package com.pardal.app.service.tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.repository.specification.MetricsSpecifications;
import com.pardal.app.service.tickets.TicketsServiceImpl;
import com.pardal.app.util.Gambiarra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketsServiceImplTest {

    @Mock
    @Gambiarra(descricao = "injetado para evitar null pointers nos testes. o ideal é Specifications ter métodos estáticos", autor = "AndreWakugawa")
    private MetricsSpecifications metricsSpecifications;

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

        when(metricsSpecifications.hasProductId(any())).thenReturn(mock(Specification.class));
        when(metricsSpecifications.hasClientId(any())).thenReturn(mock(Specification.class));

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

        when(metricsSpecifications.hasClientId(any())).thenReturn(mock(Specification.class));

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

        when(metricsSpecifications.hasProductId(any())).thenReturn(mock(Specification.class));

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

        when(ticketRepository.findAll(any(Specification.class)))
                .thenReturn(closedTickets);

        Double averageTime = ticketsService.getAverageTicketClosureTimeInHours(Specification.where(null));

        assertEquals(3.5, averageTime);

        verify(ticketRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getAverageTicketClosureTimeInHours - should return 0.0 when no closed tickets exist")
    void getAverageTicketClosureTimeInHours_whenNoTicketsExist_shouldReturnZero() {
        when(ticketRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        Double averageTime = ticketsService.getAverageTicketClosureTimeInHours(Specification.where(null));

        assertEquals(0.0, averageTime);

        verify(ticketRepository).findAll(any(Specification.class));
    }
}
