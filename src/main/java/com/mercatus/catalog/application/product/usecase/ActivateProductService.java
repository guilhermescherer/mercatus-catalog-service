package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.ActivateProductUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ActivateProductService implements ActivateProductUseCase {

    private final ProductGateway gateway;
    private final Clock clock;

    ActivateProductService(ProductGateway gateway, Clock clock) {
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Product activate(UUID id) {
        Product product = gateway.require(id);
        return gateway.mutate(product, () -> product.activate(clock.instant()));
    }
}
