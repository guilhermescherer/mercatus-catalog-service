package com.mercatus.catalog.web.category.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateCategoryRequest(@NotBlank String name, UUID parentId, long version) {
}
