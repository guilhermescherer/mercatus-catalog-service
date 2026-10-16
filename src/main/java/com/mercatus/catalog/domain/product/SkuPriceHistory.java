package com.mercatus.catalog.domain.product;

import com.mercatus.catalog.domain.shared.Money;

import java.time.Instant;
import java.util.UUID;

public record SkuPriceHistory(UUID id, UUID skuId, Money price, Instant changedAt) {

    public static SkuPriceHistory record(UUID skuId, Money price, Instant changedAt) {
        return new SkuPriceHistory(UUID.randomUUID(), skuId, price, changedAt);
    }
}
