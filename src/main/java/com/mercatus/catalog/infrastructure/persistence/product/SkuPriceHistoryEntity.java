package com.mercatus.catalog.infrastructure.persistence.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sku_price_history")
class SkuPriceHistoryEntity {

    @Id
    private UUID id;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected SkuPriceHistoryEntity() {
    }

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    UUID getSkuId() {
        return skuId;
    }

    void setSkuId(UUID skuId) {
        this.skuId = skuId;
    }

    BigDecimal getPrice() {
        return price;
    }

    void setPrice(BigDecimal price) {
        this.price = price;
    }

    Instant getChangedAt() {
        return changedAt;
    }

    void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}
