package com.mercatus.catalog.web.common;

import com.mercatus.catalog.domain.shared.PageResult;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements) {

    public static <D, T> PageResponse<T> from(PageResult<D> result, Function<D, T> mapper) {
        List<T> content = result.content().stream().map(mapper).toList();
        return new PageResponse<>(content, result.page(), result.size(), result.totalElements());
    }
}
