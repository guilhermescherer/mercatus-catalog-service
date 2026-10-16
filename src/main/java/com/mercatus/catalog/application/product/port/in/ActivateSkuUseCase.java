package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import java.util.UUID;

public interface ActivateSkuUseCase {

    Product activateSku(UUID productId, UUID skuId);
}
