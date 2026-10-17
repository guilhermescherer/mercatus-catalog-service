# mercatus-catalog-service

Microserviço dono do catálogo da plataforma Mercatus: categorias, produtos e SKUs vendáveis, com preço por SKU, histórico de preço e o estado que define se um SKU pode ser vendido.

## Funcionalidades

- `Category` com hierarquia (nome, slug único, categoria-pai opcional).
- `Product` (nome, descrição, categoria, atributos comuns, status) como raiz de agregado com seus `Sku` (código único, atributos de variação, preço em BRL, status).
- Estados `DRAFT → ACTIVE ⇄ INACTIVE` com regra de ativação (categoria + ao menos um SKU com preço) e disponibilidade de venda (SKU `ACTIVE` e Product `ACTIVE`).
- Preço por SKU em BRL com histórico consultável; nunca negativo.
- Listagem com filtros (categoria, status, texto no nome), paginação e ordenação.
- Optimistic locking (`@Version`) com conflito de versão → `409`.
- Erros em Problem Details (`404`/`409`/`422`), DTOs JSON, OpenAPI local.

## Stack

- Java 25, Spring Boot 4 (MVC)
- PostgreSQL + Flyway (`ddl-auto: validate`, schema é a fonte da verdade; atributos variáveis em JSONB)
- springdoc-openapi
- Testcontainers, JUnit 5, ArchUnit

## Estrutura

Arquitetura Hexagonal (Ports & Adapters), verificada por ArchUnit:

```
src/main/java/com/mercatus/catalog/
├── domain/           # núcleo livre de frameworks: Category, Product, Sku, Money, Slug, Status
├── application/      # casos de uso + portas (port/in, port/out) por contexto (category, product)
├── infrastructure/   # adapters de saída: persistence (JPA)
└── web/              # adapters de entrada: controllers, DTOs, Problem Details, config
```

`web` depende das portas de entrada; os casos de uso orquestram o domínio e dependem das portas de saída (`CategoryRepository`, `ProductRepository`), implementadas em `infrastructure`. O domínio não conhece nenhuma borda. ArchUnit garante: domínio sem dependência de outras camadas nem de frameworks; `application` só depende de `domain`; `@Entity` só em `infrastructure`; `@RestController` só em `web`.

## Configuração

Pré-requisitos: Java 25, Maven 3.9+, Docker, PostgreSQL 17 (via `mercatus-platform` ou Testcontainers) e acesso ao GitHub Packages para o `mercatus-parent`.

| Variável | Descrição | Padrão |
|---|---|---|
| `CATALOG_DB_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/catalog` |
| `CATALOG_DB_USER` | Usuário do banco | `mercatus` |
| `CATALOG_DB_PASSWORD` | Senha do banco | `mercatus` |
| `MAVEN_USERNAME` | Usuário do GitHub Packages | — |
| `MAVEN_PASSWORD` | Token do GitHub Packages | — |

### Rodar localmente

```bash
# Postgres via mercatus-platform (perfil core)
docker compose --profile core up -d

# Aplicação (Flyway aplica as migrations no start)
mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v1/v3/api-docs
- Health: http://localhost:8080/api/v1/actuator/health

## Endpoints (prefixo `/api/v1`)

- `POST /categories`, `GET /categories/{id}`, `GET /categories`, `PUT /categories/{id}`
- `POST /products`, `GET /products/{id}`, `GET /products`, `PUT /products/{id}`
- `POST /products/{id}:activate`, `POST /products/{id}:deactivate`
- `POST /products/{id}/skus`, `PUT /products/{id}/skus/{skuId}`
- `PUT /products/{id}/skus/{skuId}/price`, `GET /products/{id}/skus/{skuId}/price-history`
- `POST /products/{id}/skus/{skuId}:activate`, `POST /products/{id}/skus/{skuId}:deactivate`

## Modelagem e regras

- `Product` é a raiz de agregado e contém seus SKUs; a consistência de ativação é do agregado. `Category` é um agregado separado, referenciado por id.
- `SkuPriceHistory` é imutável: toda alteração de preço grava uma linha, nada é apagado.
- Ativar um `Product` exige categoria e ao menos um SKU com preço, senão `422`. Preço negativo → `422`. Slug/código de SKU duplicados → `409`.
- Cada tabela tem coluna `version` para optimistic locking; a atualização exige `version` no corpo (conflito → `409`).

## Testes

```bash
mvn test     # unitários (surefire): domínio + ArchUnit
mvn verify   # unitários + integração (failsafe, Testcontainers)
```

Cobrem: `Money`, `Slug`, regra de ativação, disponibilidade de SKU e histórico de preço; e, via Testcontainers, ativação, unicidade de slug/código, conflito de versão → 409, filtros + paginação e histórico de preço.
