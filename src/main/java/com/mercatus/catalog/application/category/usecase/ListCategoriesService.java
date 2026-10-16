package com.mercatus.catalog.application.category.usecase;

import com.mercatus.catalog.application.category.port.in.ListCategoriesUseCase;
import com.mercatus.catalog.application.category.port.out.CategoryRepository;
import com.mercatus.catalog.domain.category.Category;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.domain.shared.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ListCategoriesService implements ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    ListCategoriesService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Category> list(PageQuery pageQuery) {
        return categoryRepository.findAll(pageQuery);
    }
}
