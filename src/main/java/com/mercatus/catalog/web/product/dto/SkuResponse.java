package com.mercatus.catalog.web.product.dto;

import com.mercatus.catalog.domain.product.Sku;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SkuResponse(
        UUID id,
        String code,
        Map<String, String> variationAttributes,
        MoneyResponse price,
        String status,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static SkuResponse from(Sku sku) {
        MoneyResponse price = sku.price() == null
                ? null
                : new MoneyResponse(sku.price().amount(), sku.price().currency());
        return new SkuResponse(
                sku.id(),
                sku.code(),
                sku.variationAttributes(),
                price,
                sku.status().name(),
                sku.createdAt(),
                sku.updatedAt(),
                sku.version());
    }
}
