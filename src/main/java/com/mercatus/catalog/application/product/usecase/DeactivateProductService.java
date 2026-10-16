package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.DeactivateProductUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeactivateProductService implements DeactivateProductUseCase {

    private final ProductGateway gateway;

    DeactivateProductService(ProductGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Product deactivate(UUID id) {
        Product product = gateway.require(id);
        return gateway.mutate(product, product::deactivate);
    }
}
