package com.mercatus.catalog.domain.shared;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    public static final String CURRENCY = "BRL";

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new DomainRuleException("Price amount is required");
        }
        if (amount.signum() < 0) {
            throw new DomainRuleException("Price must not be negative");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new DomainRuleException("Price must have at most 2 decimal places");
        }
        return new Money(amount.setScale(2, RoundingMode.UNNECESSARY));
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return CURRENCY;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return CURRENCY + " " + amount.toPlainString();
    }
}
