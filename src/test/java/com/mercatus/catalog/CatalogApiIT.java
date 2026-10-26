package com.mercatus.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort
    int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        client = RestClient.builder().baseUrl("http://localhost:" + port + "/api/v1")
                .requestFactory(factory).build();
    }

    private String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createCategory() {
        return client.post()
                .uri("/categories")
                .body(Map.of("name", unique("Category")))
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createProduct(Object categoryId) {
        return client.post()
                .uri("/products")
                .body(Map.of("name", unique("Product"), "categoryId", categoryId))
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> addSku(String productId, String code, Object price) {
        Map<String, Object> body = price == null
                ? Map.of("code", code)
                : Map.of("code", code, "price", price);
        return client.post()
                .uri("/products/" + productId + "/skus")
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private HttpStatusCode statusOfPost(String uri, Object body) {
        return client.post().uri(uri).body(body)
                .exchange((request, response) -> response.getStatusCode());
    }

    private HttpStatusCode statusOfPut(String uri, Object body) {
        return client.put().uri(uri).body(body)
                .exchange((request, response) -> response.getStatusCode());
    }

    @Test
    void activatingProductWithoutCategoryIsRejectedWith422() {
        Map<String, Object> product = client.post()
                .uri("/products")
                .body(Map.of("name", unique("NoCategory")))
                .retrieve()
                .body(Map.class);
        String id = (String) product.get("id");
        addSku(id, unique("SKU"), "100.00");

        assertThat(statusOfPost("/products/" + id + ":activate", Map.of()).value()).isEqualTo(422);
    }

    @Test
    void activatingProductWithoutPricedSkuIsRejectedWith422() {
        Map<String, Object> category = createCategory();
        Map<String, Object> product = createProduct(category.get("id"));
        String id = (String) product.get("id");
        addSku(id, unique("SKU"), null);

        assertThat(statusOfPost("/products/" + id + ":activate", Map.of()).value()).isEqualTo(422);
    }

    @Test
    @SuppressWarnings("unchecked")
    void activatingProductWithCategoryAndPricedSkuSucceeds() {
        Map<String, Object> category = createCategory();
        Map<String, Object> product = createProduct(category.get("id"));
        String id = (String) product.get("id");
        addSku(id, unique("SKU"), "100.00");

        Map<String, Object> activated = client.post()
                .uri("/products/" + id + ":activate")
                .retrieve()
                .body(Map.class);
        assertThat(activated.get("status")).isEqualTo("ACTIVE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void skuAvailabilityFollowsProductAndSkuStatus() {
        Map<String, Object> category = createCategory();
        Map<String, Object> product = createProduct(category.get("id"));
        String id = (String) product.get("id");
        Map<String, Object> withSku = addSku(id, unique("SKU"), "100.00");
        List<Map<String, Object>> skus = (List<Map<String, Object>>) withSku.get("skus");
        String skuId = (String) skus.get(0).get("id");

        client.post().uri("/products/" + id + "/skus/" + skuId + ":activate").retrieve().toBodilessEntity();
        Map<String, Object> active = client.post()
                .uri("/products/" + id + ":activate").retrieve().body(Map.class);
        assertThat(active.get("status")).isEqualTo("ACTIVE");

        Map<String, Object> inactive = client.post()
                .uri("/products/" + id + ":deactivate").retrieve().body(Map.class);
        assertThat(inactive.get("status")).isEqualTo("INACTIVE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void negativePriceIsRejectedWith422() {
        Map<String, Object> category = createCategory();
        Map<String, Object> product = createProduct(category.get("id"));
        String id = (String) product.get("id");
        Map<String, Object> withSku = addSku(id, unique("SKU"), "100.00");
        List<Map<String, Object>> skus = (List<Map<String, Object>>) withSku.get("skus");
        String skuId = (String) skus.get(0).get("id");

        assertThat(statusOfPut("/products/" + id + "/skus/" + skuId + "/price",
                Map.of("price", "-1.00")).value()).isEqualTo(422);
    }

    @Test
    @SuppressWarnings("unchecked")
    void priceHistoryKeepsAllChangesInChronologicalOrder() {
        Map<String, Object> category = createCategory();
        Map<String, Object> product = createProduct(category.get("id"));
        String id = (String) product.get("id");
        Map<String, Object> withSku = addSku(id, unique("SKU"), "100.00");
        List<Map<String, Object>> skus = (List<Map<String, Object>>) withSku.get("skus");
        String skuId = (String) skus.get(0).get("id");

        client.put().uri("/products/" + id + "/skus/" + skuId + "/price")
                .body(Map.of("price", "120.00")).retrieve().toBodilessEntity();
        client.put().uri("/products/" + id + "/skus/" + skuId + "/price")
                .body(Map.of("price", "90.00")).retrieve().toBodilessEntity();

        List<Map<String, Object>> history = client.get()
                .uri("/products/" + id + "/skus/" + skuId + "/price-history")
                .retrieve()
                .body(List.class);

        assertThat(history).hasSize(3);
        assertThat(new java.math.BigDecimal(
                ((Map<String, Object>) history.get(0).get("price")).get("amount").toString()))
                .isEqualByComparingTo("100.00");
        assertThat(new java.math.BigDecimal(
                ((Map<String, Object>) history.get(2).get("price")).get("amount").toString()))
                .isEqualByComparingTo("90.00");
    }

    @Test
    void versionConflictReturns409() {
        Map<String, Object> category = createCategory();
        Map<String, Object> product = createProduct(category.get("id"));
        String id = (String) product.get("id");

        client.put().uri("/products/" + id)
                .body(Map.of("name", unique("Renamed"), "categoryId", category.get("id"), "version", 0))
                .retrieve().toBodilessEntity();

        assertThat(statusOfPut("/products/" + id,
                Map.of("name", unique("Stale"), "categoryId", category.get("id"), "version", 0))
                .value()).isEqualTo(409);
    }

    @Test
    void duplicateCategorySlugReturns409() {
        String name = unique("DupCategory");
        client.post().uri("/categories").body(Map.of("name", name)).retrieve().toBodilessEntity();

        assertThat(statusOfPost("/categories", Map.of("name", name)).value()).isEqualTo(409);
    }

    @Test
    void duplicateSkuCodeReturns409() {
        Map<String, Object> category = createCategory();
        Map<String, Object> product = createProduct(category.get("id"));
        String id = (String) product.get("id");
        String code = unique("DUP");
        addSku(id, code, "100.00");

        assertThat(statusOfPost("/products/" + id + "/skus", Map.of("code", code)).value())
                .isEqualTo(409);
    }

    @Test
    void unknownProductReturns404() {
        HttpStatusCode status = client.get().uri("/products/" + UUID.randomUUID())
                .exchange((request, response) -> response.getStatusCode());
        assertThat(status.value()).isEqualTo(404);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listSupportsFiltersAndPagination() {
        Map<String, Object> categoryA = createCategory();
        Map<String, Object> categoryB = createCategory();
        createProduct(categoryA.get("id"));
        createProduct(categoryA.get("id"));
        createProduct(categoryB.get("id"));

        Map<String, Object> page = client.get()
                .uri("/products?categoryId=" + categoryA.get("id") + "&status=DRAFT&page=0&size=1&sort=name,asc")
                .retrieve()
                .body(Map.class);

        assertThat(((Number) page.get("totalElements")).longValue()).isEqualTo(2L);
        assertThat((List<Object>) page.get("content")).hasSize(1);
        assertThat(((Number) page.get("size")).intValue()).isEqualTo(1);
        assertThat(((Number) page.get("page")).intValue()).isEqualTo(0);
    }
}
