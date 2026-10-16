package com.mercatus.catalog.infrastructure.persistence.product;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SkuPriceHistoryJpaRepository extends JpaRepository<SkuPriceHistoryEntity, UUID> {

    List<SkuPriceHistoryEntity> findBySkuIdOrderByChangedAtAsc(UUID skuId);
}
