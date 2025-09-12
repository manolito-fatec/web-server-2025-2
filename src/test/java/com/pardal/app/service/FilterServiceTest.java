package com.pardal.app.service;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Dto.FilterDataDto;
import com.pardal.app.entity.Product;
import com.pardal.app.repository.CompanyRepository;
import com.pardal.app.repository.ProductRepository;
import com.pardal.app.service.Filter.FilterService;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FilterService filterService;

    private Page<Company> companyPage;
    private Page<Product> productPage;

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
    }


    @Test
    @DisplayName("Should return FilterDataDto when valid page and size are provided")
    void getFilterData_whenValidPageAndSize_shouldReturnDto() {
        int page = 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        when(companyRepository.findAll(pageable)).thenReturn(companyPage);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        FilterDataDto result = filterService.getFilterData(page, pageSize);

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
                () -> filterService.getFilterData(invalidPage, pageSize),
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
                () -> filterService.getFilterData(page, invalidPageSize),
                "Expected IllegalArgumentException for pageSize < 1, but didn't throw."
        );

        assertEquals("O tamanho da página deve ser maior que 0", thrown.getMessage());
        verifyNoInteractions(companyRepository, productRepository);
    }
}