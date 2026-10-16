package com.mercatus.catalog.application.category.usecase;

import com.mercatus.catalog.application.category.port.in.CreateCategoryUseCase;
import com.mercatus.catalog.application.category.port.out.CategoryRepository;
import com.mercatus.catalog.application.category.usecase.support.CategoryGateway;
import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.exception.StateConflictException;
import com.mercatus.catalog.domain.category.Category;
import com.mercatus.catalog.domain.exception.DomainRuleException;
import com.mercatus.catalog.domain.exception.DuplicateSlugException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateCategoryService implements CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryGateway gateway;

    CreateCategoryService(CategoryRepository categoryRepository, CategoryGateway gateway) {
        this.categoryRepository = categoryRepository;
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Category create(CreateCategoryCommand command) {
        gateway.validateParent(command.parentId());
        try {
            Category category = Category.create(command.name(), command.parentId());
            if (categoryRepository.existsBySlug(category.slug())) {
                throw new StateConflictException("Category slug already exists: " + category.slug().value());
            }
            return categoryRepository.save(category);
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (DuplicateSlugException ex) {
            throw new StateConflictException(ex.getMessage());
        }
    }
}
