package com.mercatus.catalog.web.product.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Map;

public record CreateSkuRequest(
        @NotBlank String code, Map<String, String> variationAttributes, BigDecimal price) {
}
