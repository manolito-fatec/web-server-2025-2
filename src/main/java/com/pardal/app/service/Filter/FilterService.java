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

@Service
@RequiredArgsConstructor
public class FilterService {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;

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
