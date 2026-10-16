package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.SkuPriceHistory;
import java.util.List;
import java.util.UUID;

public interface GetSkuPriceHistoryUseCase {

    List<SkuPriceHistory> priceHistory(UUID productId, UUID skuId);
}
