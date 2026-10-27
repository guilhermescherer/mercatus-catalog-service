package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.exception.ResourceNotFoundException;
import com.mercatus.catalog.application.product.port.in.GetSkuByCodeUseCase;
import com.mercatus.catalog.application.product.port.out.ProductRepository;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.Sku;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSkuByCodeService implements GetSkuByCodeUseCase {

    private final ProductRepository productRepository;

    public GetSkuByCodeService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SkuView getByCode(String code) {
        Product product = productRepository.findBySkuCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + code));
        Sku sku = product.skus().stream()
                .filter(s -> s.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + code));
        boolean available = product.isSkuAvailableForSale(sku.id());
        return new SkuView(
                sku.code(),
                sku.price() == null ? null : sku.price().amount(),
                sku.price() == null ? null : sku.price().currency(),
                sku.status().name(),
                available,
                product.name(),
                sku.variationAttributes());
    }
}
