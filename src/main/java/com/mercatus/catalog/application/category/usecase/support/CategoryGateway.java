package com.mercatus.catalog.application.category.usecase.support;

import com.mercatus.catalog.application.category.port.out.CategoryRepository;
import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.exception.ResourceNotFoundException;
import com.mercatus.catalog.application.exception.StateConflictException;
import com.mercatus.catalog.domain.category.Category;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CategoryGateway {

    private final CategoryRepository categoryRepository;

    public CategoryGateway(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category require(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    public void validateParent(UUID parentId) {
        if (parentId != null && categoryRepository.findById(parentId).isEmpty()) {
            throw new BusinessRuleException("Parent category not found: " + parentId);
        }
    }

    public void checkVersion(Long current, long expected) {
        if (current == null || current != expected) {
            throw new StateConflictException("Version conflict for category");
        }
    }
}
