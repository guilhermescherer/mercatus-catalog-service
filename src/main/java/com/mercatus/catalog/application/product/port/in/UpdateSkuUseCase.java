package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import java.util.Map;
import java.util.UUID;

public interface UpdateSkuUseCase {

    Product updateSku(UpdateSkuCommand command);

    record UpdateSkuCommand(
            UUID productId, UUID skuId, Map<String, String> variationAttributes, long expectedVersion) {
    }
}
