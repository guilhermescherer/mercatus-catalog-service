package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface AddSkuUseCase {

    Product addSku(AddSkuCommand command);

    record AddSkuCommand(
            UUID productId, String code, Map<String, String> variationAttributes, BigDecimal price) {
    }
}
