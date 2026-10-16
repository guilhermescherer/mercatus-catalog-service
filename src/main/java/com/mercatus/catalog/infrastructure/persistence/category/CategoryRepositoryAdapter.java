package com.mercatus.catalog.infrastructure.persistence.category;

import com.mercatus.catalog.infrastructure.persistence.support.SortParser;

import com.mercatus.catalog.domain.category.Category;
import com.mercatus.catalog.application.category.port.out.CategoryRepository;
import com.mercatus.catalog.domain.exception.DuplicateSlugException;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.domain.shared.PageResult;
import com.mercatus.catalog.domain.shared.Slug;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        try {
            CategoryEntity saved = jpaRepository.saveAndFlush(CategoryMapper.toEntity(category));
            return CategoryMapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateSlugException("Category slug already exists: " + category.slug().value());
        }
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id).map(CategoryMapper::toDomain);
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        return jpaRepository.existsBySlug(slug.value());
    }

    @Override
    public PageResult<Category> findAll(PageQuery pageQuery) {
        PageRequest pageRequest = PageRequest.of(pageQuery.page(), pageQuery.size(), SortParser.parse(pageQuery.sort(), "name"));
        Page<CategoryEntity> page = jpaRepository.findAll(pageRequest);
        List<Category> content = page.getContent().stream().map(CategoryMapper::toDomain).toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
