package com.mercatus.catalog.application.category.port.in;

import com.mercatus.catalog.domain.category.Category;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.domain.shared.PageResult;

public interface ListCategoriesUseCase {

    PageResult<Category> list(PageQuery pageQuery);
}
