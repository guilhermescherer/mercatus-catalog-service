package com.mercatus.catalog.application.product.port.in;

import java.math.BigDecimal;

public interface GetSkuByCodeUseCase {

    SkuView getByCode(String code);

    record SkuView(
            String code,
            BigDecimal price,
            String currency,
            String status,
            boolean availableForSale) {
    }
}
