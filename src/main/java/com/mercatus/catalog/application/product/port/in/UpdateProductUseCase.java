package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import java.util.Map;
import java.util.UUID;

public interface UpdateProductUseCase {

    Product update(UpdateProductCommand command);

    record UpdateProductCommand(
            UUID id,
            String name,
            String description,
            UUID categoryId,
            Map<String, String> attributes,
            long expectedVersion) {
    }
}
