package com.pardal.app.repository;

import com.pardal.app.entity.Product;
import org.springframework.data.repository.Repository;

public interface ProductRepository extends Repository<Product, Integer> {
}