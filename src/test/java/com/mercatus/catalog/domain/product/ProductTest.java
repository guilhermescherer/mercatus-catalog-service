package com.mercatus.catalog.domain.product;

import com.mercatus.catalog.domain.exception.SkuNotFoundException;

import com.mercatus.catalog.domain.exception.InvalidStateTransitionException;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import com.mercatus.catalog.domain.shared.Status;

import com.mercatus.catalog.domain.shared.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductTest {

    private static Money brl(String amount) {
        return Money.of(new BigDecimal(amount));
    }

    @Test
    void activatingProductWithoutCategoryIsRejected() {
        Product product = Product.create("Shoe", null, null, Map.of());
        product.addSku("SKU-1", Map.of(), brl("100.00"));
        assertThatThrownBy(product::activate).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void activatingProductWithoutPricedSkuIsRejected() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        product.addSku("SKU-1", Map.of(), null);
        assertThatThrownBy(product::activate).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void activatingProductWithCategoryAndPricedSkuSucceeds() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        product.addSku("SKU-1", Map.of(), brl("100.00"));
        product.activate();
        assertThat(product.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void skuIsAvailableOnlyWhenProductAndSkuAreActive() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        Sku sku = product.addSku("SKU-1", Map.of(), brl("100.00"));
        product.activateSku(sku.id());
        product.activate();
        assertThat(product.isSkuAvailableForSale(sku.id())).isTrue();

        product.deactivate();
        assertThat(product.isSkuAvailableForSale(sku.id())).isFalse();
    }

    @Test
    void deactivatingDraftProductIsRejected() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        assertThatThrownBy(product::deactivate).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void changingSkuPriceProducesHistoryEntry() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        Sku sku = product.addSku("SKU-1", Map.of(), brl("100.00"));

        SkuPriceHistory first = product.changeSkuPrice(sku.id(), brl("120.00"));
        SkuPriceHistory second = product.changeSkuPrice(sku.id(), brl("90.00"));

        assertThat(first.price()).isEqualTo(brl("120.00"));
        assertThat(second.price()).isEqualTo(brl("90.00"));
        assertThat(product.findSku(sku.id()).orElseThrow().price()).isEqualTo(brl("90.00"));
    }

    @Test
    void negativeSkuPriceIsRejected() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        Sku sku = product.addSku("SKU-1", Map.of(), null);
        assertThatThrownBy(() -> product.changeSkuPrice(sku.id(), Money.of(new BigDecimal("-1.00"))))
                .isInstanceOf(DomainRuleException.class);
    }

    @Test
    void activatingSkuWithoutPriceIsRejected() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        Sku sku = product.addSku("SKU-1", Map.of(), null);
        assertThatThrownBy(() -> product.activateSku(sku.id())).isInstanceOf(DomainRuleException.class);
    }

    @Test
    void operatingOnUnknownSkuThrowsNotFound() {
        Product product = Product.create("Shoe", null, UUID.randomUUID(), Map.of());
        assertThatThrownBy(() -> product.activateSku(UUID.randomUUID()))
                .isInstanceOf(SkuNotFoundException.class);
    }
}
