package com.mercatus.catalog.infrastructure.persistence.product;

import com.mercatus.catalog.infrastructure.persistence.support.SortParser;

import com.mercatus.catalog.domain.exception.DuplicateSkuCodeException;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.domain.shared.PageResult;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.ProductFilter;
import com.mercatus.catalog.application.product.port.out.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        try {
            ProductEntity saved = jpaRepository.saveAndFlush(ProductMapper.toEntity(product));
            return ProductMapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateSkuCodeException("SKU code already exists");
        }
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id).map(ProductMapper::toDomain);
    }

    @Override
    public boolean existsBySkuCode(String code) {
        return jpaRepository.existsBySkusCode(code);
    }

    @Override
    public PageResult<Product> findAll(ProductFilter filter, PageQuery pageQuery) {
        PageRequest pageRequest = PageRequest.of(
                pageQuery.page(), pageQuery.size(), SortParser.parse(pageQuery.sort(), "name"));
        Page<ProductEntity> page = jpaRepository.findAll(toSpecification(filter), pageRequest);
        List<Product> content = page.getContent().stream().map(ProductMapper::toDomain).toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private Specification<ProductEntity> toSpecification(ProductFilter filter) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filter.categoryId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status().name()));
            }
            if (filter.nameContains() != null && !filter.nameContains().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.nameContains().toLowerCase() + "%"));
            }
            if (query != null) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
