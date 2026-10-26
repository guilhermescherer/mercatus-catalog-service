# mercatus-catalog-service

Microserviço dono do catálogo da plataforma Mercatus: categorias, produtos e SKUs vendáveis, com preço por SKU, histórico de preço e o estado que define se um SKU pode ser vendido.

## Funcionalidades

- `Category` com hierarquia (nome, slug único, categoria-pai opcional).
- `Product` (nome, descrição, categoria, atributos comuns, status) como raiz de agregado com seus `Sku` (código único, atributos de variação, preço em BRL, status).
- Estados `DRAFT → ACTIVE ⇄ INACTIVE` com regra de ativação (categoria + ao menos um SKU com preço) e disponibilidade de venda (SKU `ACTIVE` e Product `ACTIVE`).
- Preço por SKU em BRL com histórico consultável; nunca negativo.
- Listagem com filtros (categoria, status, texto no nome), paginação e ordenação.
- Consulta de SKU por código, com preço atual, status e disponibilidade de venda.
- Optimistic locking (`@Version`) com conflito de versão → `409`.
- Erros em Problem Details (`400`/`404`/`409`/`422`), DTOs JSON, OpenAPI local.

## Stack

- Java 25, Spring Boot 4.1 (MVC), via `mercatus-parent:1.0.0`
- PostgreSQL + Flyway (`ddl-auto: validate`, schema é a fonte da verdade; atributos variáveis em JSONB)
- springdoc-openapi
- Testcontainers, JUnit Jupiter 6, ArchUnit

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

Pré-requisitos: JDK 25, Maven 3.9+, PostgreSQL 17 para executar a aplicação e Docker acessível para os testes de integração. O parent `com.mercatus:mercatus-parent:1.0.0` deve estar instalado localmente ou acessível no GitHub Packages. Confira `mvn -version`: o Java usado pelo Maven deve ser 25; ajuste `JAVA_HOME` se necessário.

| Variável | Descrição | Padrão |
|---|---|---|
| `CATALOG_DB_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/catalog` |
| `CATALOG_DB_USER` | Usuário do banco | `mercatus` |
| `CATALOG_DB_PASSWORD` | Senha do banco | `mercatus` |
| `SERVER_PORT` | Porta HTTP, sem sobrescrita por argumento | `8080` |
| `MAVEN_USERNAME` | Usuário do GitHub Packages | — |
| `MAVEN_PASSWORD` | Token do GitHub Packages | — |

### Rodar localmente

Na estrutura guarda-chuva, execute a partir deste repositório. Para instalar o parent localmente:

```bash
mvn -f ../mercatus-parent/pom.xml install
```

Para obter o parent remoto, configure `MAVEN_USERNAME` e `MAVEN_PASSWORD` e acrescente `-s .mvn/settings.xml` aos comandos Maven deste serviço. Esse settings não é carregado automaticamente. Mais detalhes no [README do parent](../mercatus-parent/README.md).

Com os padrões de conexão do Platform:

```bash
docker compose -f ../mercatus-platform/compose/docker-compose.yml --profile core up -d
docker compose -f ../mercatus-platform/compose/docker-compose.yml --profile core ps
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Aguarde o PostgreSQL ficar saudável. O initdb cria `catalog` somente em volume novo; para volumes existentes ou configuração personalizada, consulte o [README do Platform](../mercatus-platform/README.md). O Flyway aplica `V1__catalog.sql` no start e o Hibernate valida o schema.

O exemplo usa a porta `8081` para permitir executar outros serviços ao mesmo tempo. Sem o argumento, o padrão é `8080`.

- Swagger UI: http://localhost:8081/api/v1/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/api/v1/v3/api-docs
- Health: http://localhost:8081/api/v1/actuator/health
- Ping: http://localhost:8081/api/v1/ping

O perfil opcional `local` desativa logs JSON. Para usá-lo, acrescente `-Dspring-boot.run.profiles=local` ao comando Maven. Por padrão, os logs usam ECS e o filtro recebe ou gera `X-Correlation-Id`, devolvendo-o na resposta.

## Endpoints (prefixo `/api/v1`)

- `POST /categories`, `GET /categories/{id}`, `GET /categories`, `PUT /categories/{id}`
- `POST /products`, `GET /products/{id}`, `GET /products`, `PUT /products/{id}`
- `POST /products/{id}:activate`, `POST /products/{id}:deactivate`
- `POST /products/{id}/skus`, `PUT /products/{id}/skus/{skuId}`
- `PUT /products/{id}/skus/{skuId}/price`, `GET /products/{id}/skus/{skuId}/price-history`
- `POST /products/{id}/skus/{skuId}:activate`, `POST /products/{id}/skus/{skuId}:deactivate`
- `GET /skus/{code}`

Listagens usam `page=0`, `size=20` e ordenação padrão `name,asc`. Produtos aceitam `categoryId`, `status` e `name`; categorias aceitam paginação e `sort`. Por exemplo:

```bash
curl 'http://localhost:8081/api/v1/products?status=ACTIVE&page=0&size=20&sort=name,asc'
```

A resposta paginada contém `content`, `page`, `size` e `totalElements`. Os contratos de request/response completos ficam no OpenAPI gerado pela aplicação. As operações ainda não exigem autenticação.

### Consulta de SKU por código

`GET /api/v1/skus/{code}` retorna `code`, `price`, `currency`, `status` do SKU e `availableForSale`. Preço e moeda são nulos quando o SKU ainda não tem preço. Código inexistente retorna `404` em Problem Details.

`availableForSale` depende de produto e SKU estarem `ACTIVE`; o saldo físico é consultado no Inventory. O futuro Order consumirá essa consulta; o Inventory não chama o Catalog nesta entrega.

## Modelagem e regras

- `Product` é a raiz de agregado e contém seus SKUs; a consistência de ativação é do agregado. `Category` é um agregado separado, referenciado por id.
- `SkuPriceHistory` é imutável: toda alteração de preço grava uma linha, nada é apagado.
- Ativar um `Product` exige categoria e ao menos um SKU com preço, senão `422`. Preço negativo → `422`. Slug/código de SKU duplicados → `409`.
- Category, Product e Sku têm `version`; o histórico de preço não tem essa coluna. `PUT /categories/{id}` e `PUT /products/{id}` recebem a versão da entidade correspondente. `PUT /products/{id}/skus/{skuId}` recebe a versão do SKU. Esses contratos comparam a versão informada e retornam `409` em conflito. Alteração de preço e ações de ativação/desativação não recebem `version` no corpo; continuam sujeitas ao controle de concorrência da persistência.

## Testes

```bash
mvn test     # unitários (surefire): domínio + ArchUnit
mvn verify   # unitários + integração (failsafe, Testcontainers)
```

Os testes de integração iniciam PostgreSQL 17 em containers próprios; o Compose não precisa estar iniciado. Sem Docker acessível, `mvn test` não valida os cenários de banco e `mvn verify` não pode ser considerado aprovado.

Cobrem: `Money`, `Slug`, regra de ativação, disponibilidade de SKU e histórico de preço; e, via Testcontainers, ativação, unicidade de slug/código, conflito de versão → 409, filtros + paginação e histórico de preço. `CatalogSkuLookupIT` cobre preço/moeda, mudança de preço, combinações de status, SKU sem preço, múltiplos SKUs, `404` e OpenAPI.

Na validação registrada em 2026-10-24, 26 testes locais passaram; a integração foi impedida pelo acesso ao Docker. Os cenários de lookup ainda dependem de execução com PostgreSQL real, conforme as [evidências da entrega](../docs/specs/inventory-phase4/validation.md).

## CI

O workflow `.github/workflows/ci.yml` executa em pushes e PRs para `main`, com JDK 25 e `mvn -B -s .mvn/settings.xml verify`. O token da execução precisa ter acesso de leitura ao pacote do parent.
