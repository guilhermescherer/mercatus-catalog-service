package com.mercatus.catalog.web.category;

import com.mercatus.catalog.application.category.port.in.CreateCategoryUseCase;
import com.mercatus.catalog.application.category.port.in.CreateCategoryUseCase.CreateCategoryCommand;
import com.mercatus.catalog.application.category.port.in.GetCategoryUseCase;
import com.mercatus.catalog.application.category.port.in.ListCategoriesUseCase;
import com.mercatus.catalog.application.category.port.in.UpdateCategoryUseCase;
import com.mercatus.catalog.application.category.port.in.UpdateCategoryUseCase.UpdateCategoryCommand;
import com.mercatus.catalog.domain.category.Category;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.web.category.dto.CategoryResponse;
import com.mercatus.catalog.web.category.dto.CreateCategoryRequest;
import com.mercatus.catalog.web.category.dto.UpdateCategoryRequest;
import com.mercatus.catalog.web.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
class CategoryController {

    private final CreateCategoryUseCase createCategory;
    private final GetCategoryUseCase getCategory;
    private final ListCategoriesUseCase listCategories;
    private final UpdateCategoryUseCase updateCategory;

    CategoryController(CreateCategoryUseCase createCategory, GetCategoryUseCase getCategory, ListCategoriesUseCase listCategories,
                       UpdateCategoryUseCase updateCategory) {
        this.createCategory = createCategory;
        this.getCategory = getCategory;
        this.listCategories = listCategories;
        this.updateCategory = updateCategory;
    }

    @PostMapping
    ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = createCategory.create(new CreateCategoryCommand(request.name(), request.parentId()));
        return ResponseEntity.created(URI.create("/categories/" + category.id()))
                .body(CategoryResponse.from(category));
    }

    @GetMapping("/{id}")
    CategoryResponse get(@PathVariable UUID id) {
        return CategoryResponse.from(getCategory.get(id));
    }

    @GetMapping
    PageResponse<CategoryResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) String sort) {
        return PageResponse.from(listCategories.list(new PageQuery(page, size, sort)), CategoryResponse::from);
    }

    @PutMapping("/{id}")
    CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        Category category = updateCategory.update(new UpdateCategoryCommand(id, request.name(), request.parentId(), request.version()));
        return CategoryResponse.from(category);
    }
}
