package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.UpdateProductUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateProductService implements UpdateProductUseCase {

    private final ProductGateway gateway;

    UpdateProductService(ProductGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Product update(UpdateProductCommand command) {
        Product product = gateway.require(command.id());
        gateway.checkVersion(product.version(), command.expectedVersion());
        return gateway.mutate(product, () -> product.update(
                command.name(), command.description(), command.categoryId(), command.attributes()));
    }
}
