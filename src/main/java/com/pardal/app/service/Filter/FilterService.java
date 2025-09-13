package com.pardal.app.service.Filter;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Dto.FilterDataDto;
import com.pardal.app.entity.Product;
import com.pardal.app.repository.CompanyRepository;
import com.pardal.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service for filtering and retrieving company and product data with pagination.
 * <p>
 * This service handles the retrieval of paginated lists of companies and products
 * from their respective repositories and combines them into a single DTO.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FilterService {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;

    /**
     * Retrieves a paginated DTO containing lists of companies and products.
     * <p>
     * This method fetches paginated data for both companies and products based on the
     * provided page number and size, combining them into a {@link FilterDataDto}.
     * </p>
     *
     * @param page the page number to retrieve (must be greater than 0)
     * @param pageSize the number of items per page (must be greater than 0)
     * @return a DTO containing paginated company and product lists
     * @throws IllegalArgumentException if the page number or page size is less than 1
     * @see FilterDataDto
     *
     * @example
     * <pre>{@code
     * // Get the first page with 20 items per page
     * FilterDataDto filterData = filterService.getFilterData(1, 20);
     * }</pre>
     */
    public FilterDataDto getFilterData(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("O número da página deve ser maior que 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("O tamanho da página deve ser maior que 0");
        }

        Pageable pageableRequest = PageRequest.of(page - 1, pageSize);

        Page<Company> companyPage = companyRepository.findAll(pageableRequest);
        Page<Product> productPage = productRepository.findAll(pageableRequest);


        return new FilterDataDto(productPage, companyPage);
    }
}