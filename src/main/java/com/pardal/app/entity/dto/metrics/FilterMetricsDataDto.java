package com.pardal.app.entity.dto.metrics;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Product;
import lombok.*;
import org.springframework.data.domain.Page;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilterMetricsDataDto {
    Page<Product> products;
    Page<Company> companies;

}
