package com.pardal.app.service.metrics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.pardal.app.entity.dto.DashboardFilterDto;
import com.pardal.app.entity.dto.TicketsByProductsCountDto;
import com.pardal.app.enums.GroupingPeriods;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.service.tickets.TicketsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Product;
import com.pardal.app.entity.dto.FilterDataDto;
import com.pardal.app.repository.CompanyRepository;
import com.pardal.app.repository.ProductRepository;
import com.pardal.app.repository.TicketStatusHistoryRepository;
import com.pardal.app.repository.specification.MetricsSpecifications;
import com.pardal.app.service.tickets.TicketsService;

@ExtendWith(MockitoExtension.class)
class MetricsServiceImplTest
{

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TicketsService ticketsService;

    @Mock
    private TicketRepository ticketsRepository;

    @Mock
    private MetricsSpecifications metricsSpecifications;

    @Mock
    private TicketStatusHistoryRepository ticketStatusHistoryRepository;

    @InjectMocks
    private TicketsServiceImpl ticketService;

    @InjectMocks
    private MetricsServiceImpl metricsService;

    private Page<Company> companyPage;
    private Page<Product> productPage;
    private DashboardFilterDto testFilters;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(1);
        company.setName("Test Company");
        List<Company> companyList = Collections.singletonList(company);
        companyPage = new PageImpl<>(companyList);

        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");
        List<Product> productList = Collections.singletonList(product);
        productPage = new PageImpl<>(productList);

        testFilters = new DashboardFilterDto();
        testFilters.setProductId(1);
        testFilters.setCustomerId(1);
        testFilters.setFromDate(LocalDateTime.now().minusDays(7));
        testFilters.setToDate(LocalDateTime.now());
        testFilters.setPeriods(GroupingPeriods.MONTH);
    }

    @Test
    @DisplayName("Should return FilterDataDto when valid page and size are provided")
    void getFilterData_whenValidPageAndSize_shouldReturnDto() {
        int page = 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        when(companyRepository.findAll(pageable)).thenReturn(companyPage);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        FilterDataDto result = metricsService.getFilterData(page, pageSize);

        assertNotNull(result);
        assertEquals(companyPage, result.getCompanies());
        assertEquals(productPage, result.getProducts());

        verify(companyRepository, times(1)).findAll(pageable);
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when page number is less than 1")
    void getFilterData_whenPageIsLessThanOne_shouldThrowException() {
        int invalidPage = 0;
        int pageSize = 10;

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getFilterData(invalidPage, pageSize),
                "Expected IllegalArgumentException for page < 1, but didn't throw."
        );

        assertEquals("O número da página deve ser maior que 0", thrown.getMessage());
        verifyNoInteractions(companyRepository, productRepository);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when page size is less than 1")
    void getFilterData_whenPageSizeIsLessThanOne_shouldThrowException() {
        int page = 1;
        int invalidPageSize = 0;

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getFilterData(page, invalidPageSize),
                "Expected IllegalArgumentException for pageSize < 1, but didn't throw."
        );

        assertEquals("O tamanho da página deve ser maior que 0", thrown.getMessage());
        verifyNoInteractions(companyRepository, productRepository);
    }

    @Test
    @DisplayName("should return zero when there are no reopened tickets")
    void testGetReopenedTicket_WhenTotalIsZero() {

        when(ticketsService.getTicketsCount(any(DashboardFilterDto.class))).thenReturn(5L);
        when(metricsSpecifications.isReOpened())
        .thenReturn((root, query, cb) -> cb.conjunction());

        when(metricsSpecifications.joinWithTicket(any(), any(), any(), any()))
        .thenReturn((root, query, cb) -> cb.conjunction());
        when(ticketStatusHistoryRepository.count(any(Specification.class))).thenReturn(0L);

        BigDecimal result = metricsService.getReopenedTicket(testFilters);

        assertEquals(BigDecimal.ZERO.setScale(6), result);
    }

}
