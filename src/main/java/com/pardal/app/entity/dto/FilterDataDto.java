package com.pardal.app.entity.dto;

import com.pardal.app.entity.Company;
import com.pardal.app.entity.Product;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilterDataDto {
    Page<Product> products;
    Page<Company> companies;

}
