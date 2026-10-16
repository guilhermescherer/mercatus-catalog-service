package com.mercatus.catalog.domain.shared;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public final class Slug {

    private final String value;

    private Slug(String value) {
        this.value = value;
    }

    public static Slug of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainRuleException("Slug source must not be blank");
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        if (normalized.isEmpty()) {
            throw new DomainRuleException("Slug source produces an empty slug");
        }
        return new Slug(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Slug slug)) {
            return false;
        }
        return value.equals(slug.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
