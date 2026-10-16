package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import java.math.BigDecimal;
import java.util.UUID;

public interface ChangeSkuPriceUseCase {

    Product changeSkuPrice(ChangeSkuPriceCommand command);

    record ChangeSkuPriceCommand(UUID productId, UUID skuId, BigDecimal price) {
    }
}
