package com.mercatus.catalog.web.product.dto;

import com.mercatus.catalog.domain.product.Product;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        Map<String, String> attributes,
        String status,
        List<SkuResponse> skus,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static ProductResponse from(Product product) {
        List<SkuResponse> skus = product.skus().stream().map(SkuResponse::from).toList();
        return new ProductResponse(
                product.id(),
                product.name(),
                product.description(),
                product.categoryId(),
                product.attributes(),
                product.status().name(),
                skus,
                product.createdAt(),
                product.updatedAt(),
                product.version());
    }
}
