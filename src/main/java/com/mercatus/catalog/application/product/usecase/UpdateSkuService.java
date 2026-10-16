package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.UpdateSkuUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateSkuService implements UpdateSkuUseCase {

    private final ProductGateway gateway;

    UpdateSkuService(ProductGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Product updateSku(UpdateSkuCommand command) {
        Product product = gateway.require(command.productId());
        gateway.checkSkuVersion(product, command.skuId(), command.expectedVersion());
        return gateway.mutate(
                product, () -> product.updateSku(command.skuId(), command.variationAttributes()));
    }
}
