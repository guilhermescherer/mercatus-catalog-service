package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.ActivateSkuUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ActivateSkuService implements ActivateSkuUseCase {

    private final ProductGateway gateway;

    ActivateSkuService(ProductGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Product activateSku(UUID productId, UUID skuId) {
        Product product = gateway.require(productId);
        return gateway.mutate(product, () -> product.activateSku(skuId));
    }
}
