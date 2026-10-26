package com.mercatus.catalog.infrastructure.persistence.product;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface ProductJpaRepository
        extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    boolean existsBySkusCode(String code);

    Optional<ProductEntity> findBySkusCode(String code);
}
