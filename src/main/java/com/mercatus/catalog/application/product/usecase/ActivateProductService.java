package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.ActivateProductUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ActivateProductService implements ActivateProductUseCase {

    private final ProductGateway gateway;

    ActivateProductService(ProductGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Product activate(UUID id) {
        Product product = gateway.require(id);
        return gateway.mutate(product, product::activate);
    }
}
