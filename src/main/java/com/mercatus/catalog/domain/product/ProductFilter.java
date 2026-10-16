package com.mercatus.catalog.domain.product;

import com.mercatus.catalog.domain.shared.Status;

import java.util.UUID;

public record ProductFilter(UUID categoryId, Status status, String nameContains) {
}
