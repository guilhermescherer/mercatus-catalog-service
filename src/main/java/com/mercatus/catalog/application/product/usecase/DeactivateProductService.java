package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.DeactivateProductUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeactivateProductService implements DeactivateProductUseCase {

    private final ProductGateway gateway;
    private final Clock clock;

    DeactivateProductService(ProductGateway gateway, Clock clock) {
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Product deactivate(UUID id) {
        Product product = gateway.require(id);
        return gateway.mutate(product, () -> product.deactivate(clock.instant()));
    }
}
