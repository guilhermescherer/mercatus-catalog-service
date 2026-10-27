package com.mercatus.catalog.web.product.dto;

import com.mercatus.catalog.application.product.port.in.GetSkuByCodeUseCase.SkuView;

import java.math.BigDecimal;
import java.util.Map;

public record SkuLookupResponse(
        String code,
        BigDecimal price,
        String currency,
        String status,
        boolean availableForSale,
        String productName,
        Map<String, String> variationAttributes) {

    public static SkuLookupResponse from(SkuView view) {
        return new SkuLookupResponse(view.code(), view.price(), view.currency(),
                view.status(), view.availableForSale(), view.productName(), view.variationAttributes());
    }
}
