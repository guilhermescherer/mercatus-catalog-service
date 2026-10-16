package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.product.port.in.CreateProductUseCase;
import com.mercatus.catalog.application.product.port.out.ProductRepository;
import com.mercatus.catalog.domain.exception.DomainRuleException;
import com.mercatus.catalog.domain.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateProductService implements CreateProductUseCase {

    private final ProductRepository productRepository;

    CreateProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Product create(CreateProductCommand command) {
        try {
            Product product = Product.create(
                    command.name(), command.description(), command.categoryId(), command.attributes());
            return productRepository.save(product);
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        }
    }
}
