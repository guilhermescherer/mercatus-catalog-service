package com.mercatus.catalog.infrastructure.persistence.category;

import com.mercatus.catalog.domain.category.Category;

final class CategoryMapper {

    private CategoryMapper() {
    }

    static CategoryEntity toEntity(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.id());
        entity.setName(category.name());
        entity.setSlug(category.slug().value());
        entity.setParentId(category.parentId());
        entity.setCreatedAt(category.createdAt());
        entity.setUpdatedAt(category.updatedAt());
        entity.setVersion(category.version());
        return entity;
    }

    static Category toDomain(CategoryEntity entity) {
        return Category.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getParentId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }
}
