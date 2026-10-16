package com.mercatus.catalog.application.product.port.in;

import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.ProductFilter;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.domain.shared.PageResult;

public interface ListProductsUseCase {

    PageResult<Product> list(ProductFilter filter, PageQuery pageQuery);
}
