package com.mercatus.catalog.domain.shared;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void negativeAmountIsRejected() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-0.01")))
                .isInstanceOf(DomainRuleException.class);
    }

    @Test
    void moreThanTwoDecimalPlacesIsRejected() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("10.123")))
                .isInstanceOf(DomainRuleException.class);
    }

    @Test
    void amountIsNormalizedToScaleTwo() {
        assertThat(Money.of(new BigDecimal("10")).amount()).isEqualByComparingTo("10.00");
        assertThat(Money.of(new BigDecimal("10")).amount().scale()).isEqualTo(2);
    }

    @Test
    void currencyIsBrl() {
        assertThat(Money.of(new BigDecimal("1.00")).currency()).isEqualTo("BRL");
    }

    @Test
    void equalityIgnoresScale() {
        assertThat(Money.of(new BigDecimal("10.0"))).isEqualTo(Money.of(new BigDecimal("10.00")));
    }
}
