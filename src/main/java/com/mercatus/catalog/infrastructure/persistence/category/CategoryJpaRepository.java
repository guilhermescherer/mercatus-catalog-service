package com.mercatus.catalog.infrastructure.persistence.category;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

    boolean existsBySlug(String slug);
}
