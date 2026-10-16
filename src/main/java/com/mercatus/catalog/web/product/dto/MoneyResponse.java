package com.mercatus.catalog.web.product.dto;

import java.math.BigDecimal;

public record MoneyResponse(BigDecimal amount, String currency) {
}
