package com.mercatus.catalog.web.category.dto;

import com.mercatus.catalog.domain.category.Category;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        UUID parentId,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.id(),
                category.name(),
                category.slug().value(),
                category.parentId(),
                category.createdAt(),
                category.updatedAt(),
                category.version());
    }
}
