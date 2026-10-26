package com.mercatus.catalog.application.product.port.out;

import com.mercatus.catalog.domain.product.ProductFilter;

import com.mercatus.catalog.domain.product.Product;

import com.mercatus.catalog.domain.shared.PageResult;

import com.mercatus.catalog.domain.shared.PageQuery;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySkuCode(String code);

    boolean existsBySkuCode(String code);

    PageResult<Product> findAll(ProductFilter filter, PageQuery pageQuery);
}
