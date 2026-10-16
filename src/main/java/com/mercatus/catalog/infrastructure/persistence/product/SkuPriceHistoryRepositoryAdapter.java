package com.mercatus.catalog.infrastructure.persistence.product;

import com.mercatus.catalog.domain.shared.Money;
import com.mercatus.catalog.domain.product.SkuPriceHistory;
import com.mercatus.catalog.application.product.port.out.SkuPriceHistoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class SkuPriceHistoryRepositoryAdapter implements SkuPriceHistoryRepository {

    private final SkuPriceHistoryJpaRepository jpaRepository;

    SkuPriceHistoryRepositoryAdapter(SkuPriceHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SkuPriceHistory save(SkuPriceHistory history) {
        SkuPriceHistoryEntity entity = new SkuPriceHistoryEntity();
        entity.setId(history.id());
        entity.setSkuId(history.skuId());
        entity.setPrice(history.price().amount());
        entity.setChangedAt(history.changedAt());
        SkuPriceHistoryEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<SkuPriceHistory> findBySkuIdOrderByChangedAt(UUID skuId) {
        return jpaRepository.findBySkuIdOrderByChangedAtAsc(skuId).stream()
                .map(SkuPriceHistoryRepositoryAdapter::toDomain)
                .toList();
    }

    private static SkuPriceHistory toDomain(SkuPriceHistoryEntity entity) {
        return new SkuPriceHistory(
                entity.getId(),
                entity.getSkuId(),
                Money.of(entity.getPrice()),
                entity.getChangedAt());
    }
}
