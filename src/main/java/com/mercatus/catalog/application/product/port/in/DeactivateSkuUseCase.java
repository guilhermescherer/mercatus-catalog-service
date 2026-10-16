package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import java.util.UUID;

public interface DeactivateSkuUseCase {

    Product deactivateSku(UUID productId, UUID skuId);
}
