package com.mercatus.catalog.application.product.port.in;

import java.math.BigDecimal;
import java.util.Map;

public interface GetSkuByCodeUseCase {

    SkuView getByCode(String code);

    record SkuView(
            String code,
            BigDecimal price,
            String currency,
            String status,
            boolean availableForSale,
            String productName,
            Map<String, String> variationAttributes) {
    }
}
