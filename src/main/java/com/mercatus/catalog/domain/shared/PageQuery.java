package com.mercatus.catalog.domain.shared;

public record PageQuery(int page, int size, String sort) {

    public PageQuery {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
    }
}
