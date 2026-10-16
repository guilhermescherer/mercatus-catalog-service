package com.mercatus.catalog.application.category.port.in;

import com.mercatus.catalog.domain.category.Category;
import java.util.UUID;

public interface CreateCategoryUseCase {

    Category create(CreateCategoryCommand command);

    record CreateCategoryCommand(String name, UUID parentId) {
    }
}
