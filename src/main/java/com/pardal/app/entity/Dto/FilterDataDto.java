package com.pardal.app.entity.Dto;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Product;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilterDataDto {
    Page<Product> products;
    Page<Company> companies;

}
