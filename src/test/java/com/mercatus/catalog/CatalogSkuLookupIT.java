package com.mercatus.catalog;

import com.mercatus.catalog.web.category.dto.CategoryResponse;
import com.mercatus.catalog.web.product.dto.ProductResponse;
import com.mercatus.catalog.web.product.dto.SkuLookupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogSkuLookupIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort
    int port;

    RestClient client;

    @BeforeEach
    void setUp() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        client = RestClient.builder().baseUrl("http://localhost:" + port + "/api/v1")
                .requestFactory(factory).build();
    }

    @Test
    void lookupReturnsCodeCurrentPriceAndCurrency() {
        SkuFixture sku = createSku("42.50");
        SkuLookupResponse response = lookup(sku.code());
        assertThat(response.code()).isEqualTo(sku.code());
        assertThat(response.price()).isEqualByComparingTo("42.50");
        assertThat(response.currency()).isEqualTo("BRL");
        ProductResponse product = client.get().uri("/products/" + sku.productId()).retrieve().body(ProductResponse.class);
        assertThat(response.productName()).isEqualTo(product.name());
        assertThat(response.variationAttributes()).isEmpty();
    }

    @Test
    void lookupReflectsPriceChanges() {
        SkuFixture sku = createSku("42.50");
        client.put().uri("/products/" + sku.productId() + "/skus/" + sku.skuId() + "/price")
                .body(Map.of("price", "51.00")).retrieve().toBodilessEntity();
        assertThat(lookup(sku.code()).price()).isEqualByComparingTo("51.00");
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, DRAFT, false",
            "DRAFT, ACTIVE, false",
            "ACTIVE, DRAFT, false",
            "ACTIVE, ACTIVE, true",
            "ACTIVE, INACTIVE, false",
            "INACTIVE, ACTIVE, false"
    })
    void availabilityRequiresBothProductAndSkuToBeActive(String productStatus, String skuStatus,
                                                        boolean expectedAvailability) {
        SkuFixture sku = createSku("42.50");
        String productPath = "/products/" + sku.productId();
        String skuPath = productPath + "/skus/" + sku.skuId();
        if (!skuStatus.equals("DRAFT")) {
            client.post().uri(skuPath + ":activate").retrieve().toBodilessEntity();
        }
        if (!productStatus.equals("DRAFT")) {
            client.post().uri(productPath + ":activate").retrieve().toBodilessEntity();
        }
        if (skuStatus.equals("INACTIVE")) {
            client.post().uri(skuPath + ":deactivate").retrieve().toBodilessEntity();
        }
        if (productStatus.equals("INACTIVE")) {
            client.post().uri(productPath + ":deactivate").retrieve().toBodilessEntity();
        }

        SkuLookupResponse response = lookup(sku.code());
        assertThat(response.status()).isEqualTo(skuStatus);
        assertThat(response.availableForSale()).isEqualTo(expectedAvailability);
    }

    @Test
    void lookupSupportsSkuWithoutPrice() {
        SkuFixture sku = createSku(null);
        SkuLookupResponse response = lookup(sku.code());
        assertThat(response.price()).isNull();
        assertThat(response.currency()).isNull();
        assertThat(response.availableForSale()).isFalse();
    }

    @Test
    void unknownCodeReturnsNotFoundProblemDetails() {
        client.get().uri(builder -> builder.pathSegment("skus")
                .queryParam("code", "{code}").build(UUID.randomUUID())).exchange((request, response) -> {
            assertThat(response.getStatusCode().value()).isEqualTo(404);
            assertThat(response.getHeaders().getContentType().toString()).startsWith("application/problem+json");
            return null;
        });
    }

    @Test
    void openApiDocumentsSkuLookup() {
        Map<?, ?> api = client.get().uri("/v3/api-docs").retrieve().body(Map.class);
        assertThat(((Map<?, ?>) api.get("paths")).containsKey("/skus/{code}")).isTrue();
        Map<?, ?> paths = (Map<?, ?>) api.get("paths");
        Map<?, ?> lookup = (Map<?, ?>) ((Map<?, ?>) paths.get("/skus")).get("get");
        java.util.List<?> parameters = (java.util.List<?>) lookup.get("parameters");
        assertThat(parameters).anySatisfy(parameter -> {
            Map<?, ?> code = (Map<?, ?>) parameter;
            assertThat(code.get("name")).isEqualTo("code");
            assertThat(code.get("in")).isEqualTo("query");
            assertThat(code.get("required")).isEqualTo(true);
        });
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) api.get("components")).get("schemas");
        Map<?, ?> properties = (Map<?, ?>) ((Map<?, ?>) schemas.get("SkuLookupResponse")).get("properties");
        assertThat(properties.containsKey("productName")).isTrue();
        assertThat(properties.containsKey("variationAttributes")).isTrue();
    }

    @Test
    void lookupSelectsTheRequestedSkuWhenTheProductHasMultipleVariations() {
        SkuFixture first = createSku("42.50");
        String secondCode = "LOOKUP-" + UUID.randomUUID();
        client.post().uri("/products/" + first.productId() + "/skus")
                .body(Map.of("code", secondCode, "price", "89.00", "variationAttributes", Map.of("size", "M", "color", "blue")))
                .retrieve().toBodilessEntity();

        assertThat(lookup(first.code()).price()).isEqualByComparingTo("42.50");
        SkuLookupResponse second = lookup(secondCode);
        assertThat(second.code()).isEqualTo(secondCode);
        assertThat(second.price()).isEqualByComparingTo("89.00");
        assertThat(second.variationAttributes()).containsExactlyInAnyOrderEntriesOf(Map.of("size", "M", "color", "blue"));
        assertThat(second.productName()).isEqualTo(lookup(first.code()).productName());
        assertThat(lookup(first.code()).variationAttributes()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOOKUP/A", "LOOKUP\\A", ".", "..", "LOOKUP +?&#% ç"})
    void queryLookupPreservesReservedCharactersAndDotSegments(String code) {
        SkuFixture sku = createSku("42.50", code);
        String productPath = "/products/" + sku.productId();
        client.post().uri(productPath + "/skus/" + sku.skuId() + ":activate")
                .retrieve().toBodilessEntity();
        client.post().uri(productPath + ":activate").retrieve().toBodilessEntity();

        SkuLookupResponse response = lookup(code);

        assertThat(response.code()).isEqualTo(code);
        assertThat(response.price()).isEqualByComparingTo("42.50");
        assertThat(response.availableForSale()).isTrue();
    }

    @Test
    void pathLookupPreservesExistingContract() {
        SkuFixture sku = createSku("42.50");
        SkuLookupResponse response = client.get().uri("/skus/" + sku.code())
                .retrieve().body(SkuLookupResponse.class);

        assertThat(response).isEqualTo(lookup(sku.code()));
    }

    private SkuLookupResponse lookup(String code) {
        return client.get().uri(builder -> builder.pathSegment("skus")
                        .queryParam("code", "{code}").build(code))
                .retrieve().body(SkuLookupResponse.class);
    }

    private SkuFixture createSku(String price) {
        return createSku(price, "LOOKUP-" + UUID.randomUUID());
    }

    private SkuFixture createSku(String price, String code) {
        CategoryResponse category = client.post().uri("/categories")
                .body(Map.of("name", "Category-" + UUID.randomUUID())).retrieve().body(CategoryResponse.class);
        ProductResponse product = client.post().uri("/products")
                .body(Map.of("name", "Product-" + UUID.randomUUID(), "categoryId", category.id()))
                .retrieve().body(ProductResponse.class);
        ProductResponse withSku = addSku(product.id(), code, price);
        return new SkuFixture(product.id(), withSku.skus().getFirst().id(), code);
    }

    private ProductResponse addSku(UUID productId, String code, String price) {
        Map<String, Object> body = price == null ? Map.of("code", code) : Map.of("code", code, "price", price);
        return client.post().uri("/products/" + productId + "/skus")
                .body(body).retrieve().body(ProductResponse.class);
    }

    private record SkuFixture(UUID productId, UUID skuId, String code) {
    }
}
