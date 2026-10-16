package com.mercatus.catalog.infrastructure.persistence.support;

import org.springframework.data.domain.Sort;

public final class SortParser {

    private SortParser() {
    }

    public static Sort parse(String sort, String defaultProperty) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, defaultProperty);
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (property.isEmpty()) {
            property = defaultProperty;
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }
}
