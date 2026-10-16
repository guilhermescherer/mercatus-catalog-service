package com.mercatus.catalog.domain.category;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import com.mercatus.catalog.domain.shared.Slug;

import java.time.Instant;
import java.util.UUID;

public class Category {

    private final UUID id;
    private String name;
    private Slug slug;
    private UUID parentId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private Category(
            UUID id,
            String name,
            Slug slug,
            UUID parentId,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Category create(String name, UUID parentId) {
        requireName(name);
        Instant now = Instant.now();
        return new Category(UUID.randomUUID(), name.trim(), Slug.of(name), parentId, now, now, null);
    }

    public static Category reconstitute(
            UUID id,
            String name,
            String slug,
            UUID parentId,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        Category category = new Category(id, name, Slug.of(slug), parentId, createdAt, updatedAt, version);
        return category;
    }

    public void update(String name, UUID parentId) {
        requireName(name);
        if (id.equals(parentId)) {
            throw new DomainRuleException("Category cannot be its own parent");
        }
        this.name = name.trim();
        this.slug = Slug.of(name);
        this.parentId = parentId;
        this.updatedAt = Instant.now();
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleException("Category name must not be blank");
        }
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Slug slug() {
        return slug;
    }

    public UUID parentId() {
        return parentId;
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
