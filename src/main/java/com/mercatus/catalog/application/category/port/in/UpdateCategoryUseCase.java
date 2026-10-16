package com.mercatus.catalog.application.category.port.in;

import com.mercatus.catalog.domain.category.Category;
import java.util.UUID;

public interface UpdateCategoryUseCase {

    Category update(UpdateCategoryCommand command);

    record UpdateCategoryCommand(UUID id, String name, UUID parentId, long expectedVersion) {
    }
}
