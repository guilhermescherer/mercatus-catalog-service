package com.mercatus.catalog.infrastructure.persistence.product;

import com.mercatus.catalog.domain.shared.Money;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.Sku;
import com.mercatus.catalog.domain.shared.Status;
import java.util.List;

final class ProductMapper {

    private ProductMapper() {
    }

    static ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.id());
        entity.setName(product.name());
        entity.setDescription(product.description());
        entity.setCategoryId(product.categoryId());
        entity.setAttributes(new java.util.LinkedHashMap<>(product.attributes()));
        entity.setStatus(product.status().name());
        entity.setCreatedAt(product.createdAt());
        entity.setUpdatedAt(product.updatedAt());
        entity.setVersion(product.version());
        List<SkuEntity> skuEntities = product.skus().stream()
                .map(ProductMapper::toSkuEntity)
                .toList();
        entity.setSkus(new java.util.ArrayList<>(skuEntities));
        return entity;
    }

    private static SkuEntity toSkuEntity(Sku sku) {
        SkuEntity entity = new SkuEntity();
        entity.setId(sku.id());
        entity.setCode(sku.code());
        entity.setVariationAttributes(new java.util.LinkedHashMap<>(sku.variationAttributes()));
        entity.setPrice(sku.price() == null ? null : sku.price().amount());
        entity.setStatus(sku.status().name());
        entity.setCreatedAt(sku.createdAt());
        entity.setUpdatedAt(sku.updatedAt());
        entity.setVersion(sku.version());
        return entity;
    }

    static Product toDomain(ProductEntity entity) {
        List<Sku> skus = entity.getSkus().stream()
                .map(ProductMapper::toSkuDomain)
                .toList();
        return Product.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategoryId(),
                entity.getAttributes(),
                Status.valueOf(entity.getStatus()),
                skus,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    private static Sku toSkuDomain(SkuEntity entity) {
        return Sku.reconstitute(
                entity.getId(),
                entity.getCode(),
                entity.getVariationAttributes(),
                entity.getPrice() == null ? null : Money.of(entity.getPrice()),
                Status.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }
}
