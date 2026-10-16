package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.exception.StateConflictException;
import com.mercatus.catalog.application.product.port.in.AddSkuUseCase;
import com.mercatus.catalog.application.product.port.out.SkuPriceHistoryRepository;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.exception.DomainRuleException;
import com.mercatus.catalog.domain.exception.DuplicateSkuCodeException;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.Sku;
import com.mercatus.catalog.domain.product.SkuPriceHistory;
import com.mercatus.catalog.domain.shared.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AddSkuService implements AddSkuUseCase {

    private final ProductGateway gateway;
    private final SkuPriceHistoryRepository priceHistoryRepository;

    AddSkuService(ProductGateway gateway, SkuPriceHistoryRepository priceHistoryRepository) {
        this.gateway = gateway;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Override
    @Transactional
    public Product addSku(AddSkuCommand command) {
        Product product = gateway.require(command.productId());
        try {
            Money money = command.price() == null ? null : Money.of(command.price());
            Sku sku = product.addSku(command.code(), command.variationAttributes(), money);
            Product saved = gateway.persist(product);
            if (money != null) {
                priceHistoryRepository.save(SkuPriceHistory.record(sku.id(), money, sku.updatedAt()));
            }
            return saved;
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (DuplicateSkuCodeException ex) {
            throw new StateConflictException(ex.getMessage());
        }
    }
}
