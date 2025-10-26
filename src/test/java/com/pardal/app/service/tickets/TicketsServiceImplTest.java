package com.pardal.app.service.tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.metrics.DashboardFilterDto;
import com.pardal.app.entity.dto.metrics.TicketCountDto;
import com.pardal.app.enums.GroupingPeriods;
import com.pardal.app.repository.TicketRepository;

import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketsServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketsServiceImpl ticketsService;

    private DashboardFilterDto testFilters;

    @BeforeEach
    void setUp() {
        testFilters = new DashboardFilterDto();
        testFilters.setProductId(1);
        testFilters.setCustomerId(100);
        testFilters.setFromDate(LocalDateTime.now().minusDays(7));
        testFilters.setToDate(LocalDateTime.now());
        testFilters.setPeriods(GroupingPeriods.DAY);
    }

    @Test
    @DisplayName("Should return count when all filters are provided")
    void getTicketsCount_whenAllFiltersAreProvided_shouldReturnCount() {
        long expectedCount = 15L;

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(testFilters);

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("Should return total count when no filters are provided")
    void getTicketsCount_whenNoFiltersAreProvided_shouldReturnCount() {
        long expectedCount = 250L;

        DashboardFilterDto emptyFilters = new DashboardFilterDto();

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(emptyFilters);

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("Should return count when a subset of filters is provided")
    void getTicketsCount_whenSomeFiltersAreProvided_shouldReturnCount() {
        long expectedCount = 42L;

        DashboardFilterDto partialFilters = new DashboardFilterDto();
        partialFilters.setCustomerId(123);
        partialFilters.setFromDate(LocalDateTime.parse("2025-09-01T00:00:00"));

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(partialFilters);

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("Should return zero when repository finds no matching tickets")
    void getTicketsCount_whenRepositoryReturnsZero_shouldReturnZero() {
        long expectedCount = 0L;

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(testFilters);

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("getTicketsCount - should handle null filters gracefully")
    void getTicketsCount_whenNullFilters_shouldHandleGracefully() {
        long expectedCount = 0L;

        when(ticketRepository.count(any(Specification.class))).thenReturn(expectedCount);

        long actualCount = ticketsService.getTicketsCount(null);

        assertEquals(expectedCount, actualCount);
        verify(ticketRepository, times(1)).count(any(Specification.class));
    }

    @Test
    @DisplayName("getTicketCountByPeriod - should return grouped ticket counts")
    void getTicketCountByPeriod_shouldReturnGroupedCounts() {
        Tickets ticket1 = new Tickets();
        ticket1.setCreatedAt(Instant.parse("2025-09-15T10:00:00Z"));

        Tickets ticket2 = new Tickets();
        ticket2.setCreatedAt(Instant.parse("2025-09-15T14:00:00Z"));

        Tickets ticket3 = new Tickets();
        ticket3.setCreatedAt(Instant.parse("2025-09-16T09:00:00Z"));

        List<Tickets> mockTickets = List.of(ticket1, ticket2, ticket3);

        when(ticketRepository.findAll(any(Specification.class))).thenReturn(mockTickets);

        List<TicketCountDto> result = ticketsService.getTicketCountByPeriod(testFilters);

        assertNotNull(result);
        verify(ticketRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getTicketCountByPeriod - should return empty list when no tickets found")
    void getTicketCountByPeriod_whenNoTickets_shouldReturnEmptyList() {
        when(ticketRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        List<TicketCountDto> result = ticketsService.getTicketCountByPeriod(testFilters);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(ticketRepository, times(1)).findAll(any(Specification.class));
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
