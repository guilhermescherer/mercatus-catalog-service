package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import java.util.Map;
import java.util.UUID;

public interface CreateProductUseCase {

    Product create(CreateProductCommand command);

    record CreateProductCommand(
            String name, String description, UUID categoryId, Map<String, String> attributes) {
    }
}
