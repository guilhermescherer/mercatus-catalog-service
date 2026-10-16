package com.mercatus.catalog.domain.product;

import com.mercatus.catalog.domain.exception.SkuNotFoundException;

import com.mercatus.catalog.domain.exception.InvalidStateTransitionException;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import com.mercatus.catalog.domain.shared.Status;

import com.mercatus.catalog.domain.shared.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Product {

    private final UUID id;
    private String name;
    private String description;
    private UUID categoryId;
    private Map<String, String> attributes;
    private Status status;
    private final List<Sku> skus;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private Product(
            UUID id,
            String name,
            String description,
            UUID categoryId,
            Map<String, String> attributes,
            Status status,
            List<Sku> skus,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.attributes = new LinkedHashMap<>(attributes);
        this.status = status;
        this.skus = new ArrayList<>(skus);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Product create(
            String name, String description, UUID categoryId, Map<String, String> attributes) {
        requireName(name);
        Instant now = Instant.now();
        return new Product(
                UUID.randomUUID(),
                name.trim(),
                description,
                categoryId,
                attributes == null ? Map.of() : attributes,
                Status.DRAFT,
                List.of(),
                now,
                now,
                null);
    }

    public static Product reconstitute(
            UUID id,
            String name,
            String description,
            UUID categoryId,
            Map<String, String> attributes,
            Status status,
            List<Sku> skus,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        return new Product(
                id, name, description, categoryId, attributes, status, skus, createdAt, updatedAt, version);
    }

    public void update(String name, String description, UUID categoryId, Map<String, String> attributes) {
        requireName(name);
        this.name = name.trim();
        this.description = description;
        this.categoryId = categoryId;
        this.attributes = new LinkedHashMap<>(attributes == null ? Map.of() : attributes);
        this.updatedAt = Instant.now();
    }

    public Sku addSku(String code, Map<String, String> variationAttributes, Money price) {
        Sku sku = Sku.create(code, variationAttributes, price);
        skus.add(sku);
        this.updatedAt = Instant.now();
        return sku;
    }

    public void updateSku(UUID skuId, Map<String, String> variationAttributes) {
        requireSku(skuId).update(variationAttributes);
        this.updatedAt = Instant.now();
    }

    public SkuPriceHistory changeSkuPrice(UUID skuId, Money newPrice) {
        SkuPriceHistory history = requireSku(skuId).changePrice(newPrice);
        this.updatedAt = Instant.now();
        return history;
    }

    public void activateSku(UUID skuId) {
        requireSku(skuId).activate();
        this.updatedAt = Instant.now();
    }

    public void deactivateSku(UUID skuId) {
        requireSku(skuId).deactivate();
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (status == Status.ACTIVE) {
            return;
        }
        if (categoryId == null) {
            throw new InvalidStateTransitionException("Product without a category cannot be activated");
        }
        boolean hasPricedSku = skus.stream().anyMatch(Sku::hasPrice);
        if (!hasPricedSku) {
            throw new InvalidStateTransitionException(
                    "Product without a priced SKU cannot be activated");
        }
        this.status = Status.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (status == Status.INACTIVE) {
            return;
        }
        if (status != Status.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Only an active product can be deactivated");
        }
        this.status = Status.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isSkuAvailableForSale(UUID skuId) {
        return status == Status.ACTIVE && requireSku(skuId).isActive();
    }

    private Sku requireSku(UUID skuId) {
        return findSku(skuId)
                .orElseThrow(() -> new SkuNotFoundException("SKU not found: " + skuId));
    }

    public Optional<Sku> findSku(UUID skuId) {
        return skus.stream().filter(sku -> sku.id().equals(skuId)).findFirst();
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleException("Product name must not be blank");
        }
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public UUID categoryId() {
        return categoryId;
    }

    public Map<String, String> attributes() {
        return Map.copyOf(attributes);
    }

    public Status status() {
        return status;
    }

    public List<Sku> skus() {
        return List.copyOf(skus);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Long version() {
        return version;
    }
}
