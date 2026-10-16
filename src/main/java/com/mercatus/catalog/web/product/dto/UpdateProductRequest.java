package com.mercatus.catalog.web.product.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record UpdateProductRequest(
        @NotBlank String name,
        String description,
        UUID categoryId,
        Map<String, String> attributes,
        long version) {
}
