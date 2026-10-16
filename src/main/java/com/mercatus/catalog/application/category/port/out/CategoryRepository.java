package com.mercatus.catalog.application.category.port.out;

import com.mercatus.catalog.domain.category.Category;

import com.mercatus.catalog.domain.shared.PageResult;

import com.mercatus.catalog.domain.shared.PageQuery;

import com.mercatus.catalog.domain.shared.Slug;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    boolean existsBySlug(Slug slug);

    PageResult<Category> findAll(PageQuery pageQuery);
}
