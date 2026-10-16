package com.mercatus.catalog.web.product.dto;

import java.util.Map;

public record UpdateSkuRequest(Map<String, String> variationAttributes, long version) {
}
