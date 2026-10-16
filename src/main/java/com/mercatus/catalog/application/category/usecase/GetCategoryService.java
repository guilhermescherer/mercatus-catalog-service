package com.mercatus.catalog.application.category.usecase;

import com.mercatus.catalog.application.category.port.in.GetCategoryUseCase;
import com.mercatus.catalog.application.category.usecase.support.CategoryGateway;
import com.mercatus.catalog.domain.category.Category;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetCategoryService implements GetCategoryUseCase {

    private final CategoryGateway gateway;

    GetCategoryService(CategoryGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Category get(UUID id) {
        return gateway.require(id);
    }
}
