package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.DeactivateSkuUseCase;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeactivateSkuService implements DeactivateSkuUseCase {

    private final ProductGateway gateway;
    private final Clock clock;

    DeactivateSkuService(ProductGateway gateway, Clock clock) {
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Product deactivateSku(UUID productId, UUID skuId) {
        Product product = gateway.require(productId);
        return gateway.mutate(product, () -> product.deactivateSku(skuId, clock.instant()));
    }
}
