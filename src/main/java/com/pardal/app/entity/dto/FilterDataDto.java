package com.pardal.app.entity.dto;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Product;
import lombok.*;
import org.springframework.data.domain.Page;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilterDataDto {
    Page<Product> products;
    Page<Company> companies;

}
