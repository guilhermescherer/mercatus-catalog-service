package com.mercatus.catalog.web.product;

import com.mercatus.catalog.application.product.port.in.ActivateProductUseCase;
import com.mercatus.catalog.application.product.port.in.ActivateSkuUseCase;
import com.mercatus.catalog.application.product.port.in.AddSkuUseCase;
import com.mercatus.catalog.application.product.port.in.AddSkuUseCase.AddSkuCommand;
import com.mercatus.catalog.application.product.port.in.ChangeSkuPriceUseCase;
import com.mercatus.catalog.application.product.port.in.ChangeSkuPriceUseCase.ChangeSkuPriceCommand;
import com.mercatus.catalog.application.product.port.in.CreateProductUseCase;
import com.mercatus.catalog.application.product.port.in.CreateProductUseCase.CreateProductCommand;
import com.mercatus.catalog.application.product.port.in.DeactivateProductUseCase;
import com.mercatus.catalog.application.product.port.in.DeactivateSkuUseCase;
import com.mercatus.catalog.application.product.port.in.GetProductUseCase;
import com.mercatus.catalog.application.product.port.in.GetSkuPriceHistoryUseCase;
import com.mercatus.catalog.application.product.port.in.ListProductsUseCase;
import com.mercatus.catalog.application.product.port.in.UpdateProductUseCase;
import com.mercatus.catalog.application.product.port.in.UpdateProductUseCase.UpdateProductCommand;
import com.mercatus.catalog.application.product.port.in.UpdateSkuUseCase;
import com.mercatus.catalog.application.product.port.in.UpdateSkuUseCase.UpdateSkuCommand;
import com.mercatus.catalog.domain.product.Product;
import com.mercatus.catalog.domain.product.ProductFilter;
import com.mercatus.catalog.domain.shared.PageQuery;
import com.mercatus.catalog.domain.shared.Status;
import com.mercatus.catalog.web.common.PageResponse;
import com.mercatus.catalog.web.product.dto.ChangePriceRequest;
import com.mercatus.catalog.web.product.dto.CreateProductRequest;
import com.mercatus.catalog.web.product.dto.CreateSkuRequest;
import com.mercatus.catalog.web.product.dto.PriceHistoryResponse;
import com.mercatus.catalog.web.product.dto.ProductResponse;
import com.mercatus.catalog.web.product.dto.UpdateProductRequest;
import com.mercatus.catalog.web.product.dto.UpdateSkuRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class ProductController {

    private final CreateProductUseCase createProduct;
    private final GetProductUseCase getProduct;
    private final ListProductsUseCase listProducts;
    private final UpdateProductUseCase updateProduct;
    private final ActivateProductUseCase activateProduct;
    private final DeactivateProductUseCase deactivateProduct;
    private final AddSkuUseCase addSku;
    private final UpdateSkuUseCase updateSku;
    private final ChangeSkuPriceUseCase changeSkuPrice;
    private final GetSkuPriceHistoryUseCase getSkuPriceHistory;
    private final ActivateSkuUseCase activateSku;
    private final DeactivateSkuUseCase deactivateSku;

    ProductController(
            CreateProductUseCase createProduct,
            GetProductUseCase getProduct,
            ListProductsUseCase listProducts,
            UpdateProductUseCase updateProduct,
            ActivateProductUseCase activateProduct,
            DeactivateProductUseCase deactivateProduct,
            AddSkuUseCase addSku,
            UpdateSkuUseCase updateSku,
            ChangeSkuPriceUseCase changeSkuPrice,
            GetSkuPriceHistoryUseCase getSkuPriceHistory,
            ActivateSkuUseCase activateSku,
            DeactivateSkuUseCase deactivateSku) {
        this.createProduct = createProduct;
        this.getProduct = getProduct;
        this.listProducts = listProducts;
        this.updateProduct = updateProduct;
        this.activateProduct = activateProduct;
        this.deactivateProduct = deactivateProduct;
        this.addSku = addSku;
        this.updateSku = updateSku;
        this.changeSkuPrice = changeSkuPrice;
        this.getSkuPriceHistory = getSkuPriceHistory;
        this.activateSku = activateSku;
        this.deactivateSku = deactivateSku;
    }

    @PostMapping
    ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        Product product = createProduct.create(new CreateProductCommand(
                request.name(), request.description(), request.categoryId(), request.attributes()));
        return ResponseEntity.created(URI.create("/products/" + product.id()))
                .body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    ProductResponse get(@PathVariable UUID id) {
        return ProductResponse.from(getProduct.get(id));
    }

    @GetMapping
    PageResponse<ProductResponse> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        ProductFilter filter = new ProductFilter(categoryId, status, name);
        return PageResponse.from(
                listProducts.list(filter, new PageQuery(page, size, sort)), ProductResponse::from);
    }

    @PutMapping("/{id}")
    ProductResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        Product product = updateProduct.update(new UpdateProductCommand(
                id, request.name(), request.description(), request.categoryId(),
                request.attributes(), request.version()));
        return ProductResponse.from(product);
    }

    @PostMapping("/{id}:activate")
    ProductResponse activate(@PathVariable UUID id) {
        return ProductResponse.from(activateProduct.activate(id));
    }

    @PostMapping("/{id}:deactivate")
    ProductResponse deactivate(@PathVariable UUID id) {
        return ProductResponse.from(deactivateProduct.deactivate(id));
    }

    @PostMapping("/{id}/skus")
    ResponseEntity<ProductResponse> addSku(
            @PathVariable UUID id, @Valid @RequestBody CreateSkuRequest request) {
        Product product = addSku.addSku(new AddSkuCommand(
                id, request.code(), request.variationAttributes(), request.price()));
        return ResponseEntity.created(URI.create("/products/" + id + "/skus"))
                .body(ProductResponse.from(product));
    }

    @PutMapping("/{id}/skus/{skuId}")
    ProductResponse updateSku(
            @PathVariable UUID id,
            @PathVariable UUID skuId,
            @Valid @RequestBody UpdateSkuRequest request) {
        Product product = updateSku.updateSku(new UpdateSkuCommand(
                id, skuId, request.variationAttributes(), request.version()));
        return ProductResponse.from(product);
    }

    @PutMapping("/{id}/skus/{skuId}/price")
    ProductResponse changeSkuPrice(
            @PathVariable UUID id,
            @PathVariable UUID skuId,
            @Valid @RequestBody ChangePriceRequest request) {
        return ProductResponse.from(
                changeSkuPrice.changeSkuPrice(new ChangeSkuPriceCommand(id, skuId, request.price())));
    }

    @GetMapping("/{id}/skus/{skuId}/price-history")
    List<PriceHistoryResponse> priceHistory(@PathVariable UUID id, @PathVariable UUID skuId) {
        return getSkuPriceHistory.priceHistory(id, skuId).stream()
                .map(PriceHistoryResponse::from)
                .toList();
    }

    @PostMapping("/{id}/skus/{skuId}:activate")
    ProductResponse activateSku(@PathVariable UUID id, @PathVariable UUID skuId) {
        return ProductResponse.from(activateSku.activateSku(id, skuId));
    }

    @PostMapping("/{id}/skus/{skuId}:deactivate")
    ProductResponse deactivateSku(@PathVariable UUID id, @PathVariable UUID skuId) {
        return ProductResponse.from(deactivateSku.deactivateSku(id, skuId));
    }
}
