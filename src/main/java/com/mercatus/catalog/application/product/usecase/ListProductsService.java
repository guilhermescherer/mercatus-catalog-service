package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.ListProductsUseCase;
import com.mercatus.catalog.application.product.port.out.ProductRepository;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.ProductFilter;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.domain.shared.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ListProductsService implements ListProductsUseCase {

    private final ProductRepository productRepository;

    ListProductsService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> list(ProductFilter filter, PageQuery pageQuery) {
        return productRepository.findAll(filter, pageQuery);
    }
}
