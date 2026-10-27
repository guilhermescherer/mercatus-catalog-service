package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.product.port.in.CreateProductUseCase;
import com.mercatus.catalog.application.product.port.out.ProductRepository;
import com.mercatus.catalog.domain.exception.DomainRuleException;
import com.mercatus.catalog.domain.product.Product;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateProductService implements CreateProductUseCase {

    private final ProductRepository productRepository;
    private final Clock clock;
    private final Supplier<UUID> identifiers;

    CreateProductService(ProductRepository productRepository, Clock clock, Supplier<UUID> identifiers) {
        this.productRepository = productRepository;
        this.clock = clock;
        this.identifiers = identifiers;
    }

    @Override
    @Transactional
    public Product create(CreateProductCommand command) {
        try {
            Product product = Product.create(
                    identifiers.get(), command.name(), command.description(), command.categoryId(),
                    command.attributes(), clock.instant());
            return productRepository.save(product);
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        }
    }
}
