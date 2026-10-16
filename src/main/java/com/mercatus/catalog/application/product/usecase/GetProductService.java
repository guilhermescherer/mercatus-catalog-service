package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.GetProductUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetProductService implements GetProductUseCase {

    private final ProductGateway gateway;

    GetProductService(ProductGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Product get(UUID id) {
        return gateway.require(id);
    }
}
