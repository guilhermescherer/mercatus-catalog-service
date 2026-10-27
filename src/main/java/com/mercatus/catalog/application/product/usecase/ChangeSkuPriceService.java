package com.mercatus.catalog.application.product.usecase;

import com.mercatus.catalog.application.exception.BusinessRuleException;
import com.mercatus.catalog.application.exception.ResourceNotFoundException;
import com.mercatus.catalog.application.product.port.in.ChangeSkuPriceUseCase;
import com.mercatus.catalog.application.product.port.out.SkuPriceHistoryRepository;
import com.mercatus.catalog.application.product.usecase.support.ProductGateway;
import com.mercatus.catalog.domain.exception.DomainRuleException;
import com.mercatus.catalog.domain.exception.SkuNotFoundException;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.SkuPriceHistory;
import com.mercatus.catalog.domain.shared.Money;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ChangeSkuPriceService implements ChangeSkuPriceUseCase {

    private final ProductGateway gateway;
    private final SkuPriceHistoryRepository priceHistoryRepository;
    private final Clock clock;

    ChangeSkuPriceService(ProductGateway gateway, SkuPriceHistoryRepository priceHistoryRepository, Clock clock) {
        this.gateway = gateway;
        this.priceHistoryRepository = priceHistoryRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Product changeSkuPrice(ChangeSkuPriceCommand command) {
        Product product = gateway.require(command.productId());
        try {
            Money money = Money.of(command.price());
            SkuPriceHistory history = product.changeSkuPrice(command.skuId(), money, clock.instant());
            Product saved = gateway.persist(product);
            priceHistoryRepository.save(history);
            return saved;
        } catch (SkuNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        }
    }
}
