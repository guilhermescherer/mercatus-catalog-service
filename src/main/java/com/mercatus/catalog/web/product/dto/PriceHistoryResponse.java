package com.mercatus.catalog.web.product.dto;

import com.mercatus.catalog.domain.product.SkuPriceHistory;
import java.time.Instant;
import java.util.UUID;

public record PriceHistoryResponse(UUID id, UUID skuId, MoneyResponse price, Instant changedAt) {

    public static PriceHistoryResponse from(SkuPriceHistory history) {
        return new PriceHistoryResponse(
                history.id(),
                history.skuId(),
                new MoneyResponse(history.price().amount(), history.price().currency()),
                history.changedAt());
    }
}
