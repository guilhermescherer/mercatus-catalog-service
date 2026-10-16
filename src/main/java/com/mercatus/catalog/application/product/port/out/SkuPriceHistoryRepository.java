package com.mercatus.catalog.application.product.port.out;

import com.mercatus.catalog.domain.product.SkuPriceHistory;

import java.util.List;
import java.util.UUID;

public interface SkuPriceHistoryRepository {

    SkuPriceHistory save(SkuPriceHistory history);

    List<SkuPriceHistory> findBySkuIdOrderByChangedAt(UUID skuId);
}
