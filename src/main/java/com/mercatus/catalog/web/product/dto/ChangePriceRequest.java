package com.mercatus.catalog.web.product.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ChangePriceRequest(@NotNull BigDecimal price) {
}
