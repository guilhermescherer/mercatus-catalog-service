package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.product.port.in.GetSkuPriceHistoryUseCase;
import com.mercatus.catalog.application.product.port.out.SkuPriceHistoryRepository;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.SkuPriceHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetSkuPriceHistoryService implements GetSkuPriceHistoryUseCase {

    private final ProductGateway gateway;
    private final SkuPriceHistoryRepository priceHistoryRepository;

    GetSkuPriceHistoryService(ProductGateway gateway, SkuPriceHistoryRepository priceHistoryRepository) {
        this.gateway = gateway;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkuPriceHistory> priceHistory(UUID productId, UUID skuId) {
        Product product = gateway.require(productId);
        gateway.requireSku(product, skuId);
        return priceHistoryRepository.findBySkuIdOrderByChangedAt(skuId);
    }
}
