package com.mercatus.catalog.domain.product;

import com.mercatus.catalog.domain.exception.SkuNotFoundException;

import com.mercatus.catalog.domain.exception.InvalidStateTransitionException;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import com.mercatus.catalog.domain.shared.Status;

import com.mercatus.catalog.domain.shared.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductTest {

    private static final Instant NOW = Instant.parse("2026-10-27T12:00:00Z");

    private static Money brl(String amount) {
        return Money.of(new BigDecimal(amount));
    }

    private static Product product(UUID categoryId) {
        return Product.create(UUID.randomUUID(), "Shoe", null, categoryId, Map.of(), NOW);
    }

    private static Sku addSku(Product product, Money price) {
        return product.addSku(UUID.randomUUID(), "SKU-1", Map.of(), price, NOW);
    }

    @Test
    void activatingProductWithoutCategoryIsRejected() {
        Product product = product(null);
        addSku(product, brl("100.00"));
        assertThatThrownBy(() -> product.activate(NOW)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void activatingProductWithoutPricedSkuIsRejected() {
        Product product = product(UUID.randomUUID());
        addSku(product, null);
        assertThatThrownBy(() -> product.activate(NOW)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void activatingProductWithCategoryAndPricedSkuSucceeds() {
        Product product = product(UUID.randomUUID());
        addSku(product, brl("100.00"));
        product.activate(NOW);
        assertThat(product.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void skuIsAvailableOnlyWhenProductAndSkuAreActive() {
        Product product = product(UUID.randomUUID());
        Sku sku = addSku(product, brl("100.00"));
        product.activateSku(sku.id(), NOW);
        product.activate(NOW);
        assertThat(product.isSkuAvailableForSale(sku.id())).isTrue();

        product.deactivate(NOW);
        assertThat(product.isSkuAvailableForSale(sku.id())).isFalse();
    }

    @Test
    void deactivatingDraftProductIsRejected() {
        Product product = product(UUID.randomUUID());
        assertThatThrownBy(() -> product.deactivate(NOW)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void changingSkuPriceProducesHistoryEntry() {
        Product product = product(UUID.randomUUID());
        Sku sku = addSku(product, brl("100.00"));

        SkuPriceHistory first = product.changeSkuPrice(sku.id(), brl("120.00"), NOW);
        SkuPriceHistory second = product.changeSkuPrice(sku.id(), brl("90.00"), NOW);

        assertThat(first.price()).isEqualTo(brl("120.00"));
        assertThat(second.price()).isEqualTo(brl("90.00"));
        assertThat(product.findSku(sku.id()).orElseThrow().price()).isEqualTo(brl("90.00"));
    }

    @Test
    void negativeSkuPriceIsRejected() {
        Product product = product(UUID.randomUUID());
        Sku sku = addSku(product, null);
        assertThatThrownBy(() -> product.changeSkuPrice(sku.id(), Money.of(new BigDecimal("-1.00")), NOW))
                .isInstanceOf(DomainRuleException.class);
    }

    @Test
    void activatingSkuWithoutPriceIsRejected() {
        Product product = product(UUID.randomUUID());
        Sku sku = addSku(product, null);
        assertThatThrownBy(() -> product.activateSku(sku.id(), NOW)).isInstanceOf(DomainRuleException.class);
    }

    @Test
    void operatingOnUnknownSkuThrowsNotFound() {
        Product product = product(UUID.randomUUID());
        assertThatThrownBy(() -> product.activateSku(UUID.randomUUID(), NOW))
                .isInstanceOf(SkuNotFoundException.class);
    }
}
