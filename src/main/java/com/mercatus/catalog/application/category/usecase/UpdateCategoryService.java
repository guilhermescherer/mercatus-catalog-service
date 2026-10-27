package com.mercatus.catalog.application.category.usecase;

import com.mercatus.catalog.application.category.port.in.UpdateCategoryUseCase;
import com.mercatus.catalog.application.category.port.out.CategoryRepository;
import com.mercatus.catalog.application.category.usecase.support.CategoryGateway;
import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.exception.StateConflictException;
import com.mercatus.catalog.domain.category.Category;
import com.mercatus.catalog.domain.exception.DomainRuleException;
import com.mercatus.catalog.domain.exception.DuplicateSlugException;
import com.mercatus.catalog.domain.shared.Slug;
import java.time.Clock;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateCategoryService implements UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryGateway gateway;
    private final Clock clock;

    UpdateCategoryService(CategoryRepository categoryRepository, CategoryGateway gateway, Clock clock) {
        this.categoryRepository = categoryRepository;
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Category update(UpdateCategoryCommand command) {
        Category category = gateway.require(command.id());
        gateway.checkVersion(category.version(), command.expectedVersion());
        gateway.validateParent(command.parentId());
        Slug previousSlug = category.slug();
        try {
            category.update(command.name(), command.parentId(), clock.instant());
            if (!category.slug().equals(previousSlug) && categoryRepository.existsBySlug(category.slug())) {
                throw new StateConflictException("Category slug already exists: " + category.slug().value());
            }
            return categoryRepository.save(category);
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (DuplicateSlugException ex) {
            throw new StateConflictException(ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Category was modified concurrently");
        }
    }
}
