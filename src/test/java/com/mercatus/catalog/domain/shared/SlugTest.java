package com.mercatus.catalog.domain.shared;

import com.mercatus.catalog.domain.exception.DomainRuleException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SlugTest {

    @Test
    void normalizesAccentsSpacesAndCase() {
        assertThat(Slug.of("Calçados Esportivos").value()).isEqualTo("calcados-esportivos");
    }

    @Test
    void trimsLeadingAndTrailingSeparators() {
        assertThat(Slug.of("  --Hello World!!  ").value()).isEqualTo("hello-world");
    }

    @Test
    void blankSourceIsRejected() {
        assertThatThrownBy(() -> Slug.of("   ")).isInstanceOf(DomainRuleException.class);
    }

    @Test
    void sourceWithoutAlphanumericIsRejected() {
        assertThatThrownBy(() -> Slug.of("!!!")).isInstanceOf(DomainRuleException.class);
    }
}
