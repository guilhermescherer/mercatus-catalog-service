package com.mercatus.catalog.domain.product;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import com.mercatus.catalog.domain.shared.Status;

import com.mercatus.catalog.domain.shared.Money;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Sku {

    private final UUID id;
    private String code;
    private Map<String, String> variationAttributes;
    private Money price;
    private Status status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private Sku(
            UUID id,
            String code,
            Map<String, String> variationAttributes,
            Money price,
            Status status,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        this.id = id;
        this.code = code;
        this.variationAttributes = new LinkedHashMap<>(variationAttributes);
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    static Sku create(UUID id, String code, Map<String, String> variationAttributes, Money price, Instant now) {
        requireCode(code);
        return new Sku(
                id,
                code.trim(),
                variationAttributes == null ? Map.of() : variationAttributes,
                price,
                Status.DRAFT,
                now,
                now,
                null);
    }

    public static Sku reconstitute(
            UUID id,
            String code,
            Map<String, String> variationAttributes,
            Money price,
            Status status,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        return new Sku(
                id,
                code,
                variationAttributes == null ? Map.of() : variationAttributes,
                price,
                status,
                createdAt,
                updatedAt,
                version);
    }

    void update(Map<String, String> variationAttributes, Instant now) {
        this.variationAttributes = new LinkedHashMap<>(
                variationAttributes == null ? Map.of() : variationAttributes);
        this.updatedAt = now;
    }

    SkuPriceHistory changePrice(Money newPrice, Instant now) {
        if (newPrice == null) {
            throw new DomainRuleException("Price is required");
        }
        this.price = newPrice;
        this.updatedAt = now;
        return SkuPriceHistory.record(id, newPrice, now);
    }

    void activate(Instant now) {
        if (status == Status.ACTIVE) {
            return;
        }
        if (price == null) {
            throw new DomainRuleException("SKU without a price cannot be activated");
        }
        this.status = Status.ACTIVE;
        this.updatedAt = now;
    }

    void deactivate(Instant now) {
        if (status == Status.INACTIVE) {
            return;
        }
        this.status = Status.INACTIVE;
        this.updatedAt = now;
    }

    public boolean hasPrice() {
        return price != null;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    private static void requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainRuleException("SKU code must not be blank");
        }
    }

    public UUID id() {
        return id;
    }

    public String code() {
        return code;
    }

    public Map<String, String> variationAttributes() {
        return Map.copyOf(variationAttributes);
    }

    public Money price() {
        return price;
    }

    public Status status() {
        return status;
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
