package com.mercatus.catalog.application.product.usecase.support;

import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.exception.ResourceNotFoundException;
import com.mercatus.catalog.application.exception.StateConflictException;
import com.mercatus.catalog.application.product.port.out.ProductRepository;
import com.mercatus.catalog.domain.exception.DomainRuleException;
import com.mercatus.catalog.domain.exception.DuplicateSkuCodeException;
import com.mercatus.catalog.domain.exception.InvalidStateTransitionException;
import com.mercatus.catalog.domain.exception.SkuNotFoundException;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.Sku;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
public class ProductGateway {

    private final ProductRepository productRepository;

    public ProductGateway(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product require(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public Product mutate(Product product, Runnable change) {
        try {
            change.run();
            return persist(product);
        } catch (SkuNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (InvalidStateTransitionException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        }
    }

    public Product persist(Product product) {
        try {
            return productRepository.save(product);
        } catch (DuplicateSkuCodeException ex) {
            throw new StateConflictException(ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Product was modified concurrently");
        }
    }

    public void checkVersion(Long current, long expected) {
        if (current == null || current != expected) {
            throw new StateConflictException("Version conflict for product");
        }
    }

    public Sku requireSku(Product product, UUID skuId) {
        return product.findSku(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + skuId));
    }

    public void checkSkuVersion(Product product, UUID skuId, long expected) {
        Sku sku = requireSku(product, skuId);
        if (sku.version() == null || sku.version() != expected) {
            throw new StateConflictException("Version conflict for SKU");
        }
    }
}
