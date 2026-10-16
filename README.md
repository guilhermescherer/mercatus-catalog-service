# 🚀 mercatus-catalog-service

Microserviço dono do catálogo da plataforma Mercatus: categorias, produtos e
SKUs vendáveis, com preço por SKU, histórico de preço e o estado que define se
um SKU pode ser vendido.

## 📋 Funcionalidades

- `Category` com hierarquia (nome, slug único, categoria-pai opcional).
- `Product` (nome, descrição, categoria, atributos comuns, status) e seus SKUs.
- `Sku` (código único, atributos de variação, preço em BRL, status).
- Estados `DRAFT → ACTIVE ⇄ INACTIVE` com a regra de ativação (categoria + SKU com preço).
- Disponibilidade de SKU para venda (SKU `ACTIVE` e Product `ACTIVE`).
- Preço por SKU em BRL com histórico consultável por data.
- Listagem com filtros (categoria, status, texto no nome), paginação e ordenação.
- Optimistic locking (`@Version`) com conflito de versão → `409`.
- Erros em Problem Details (`404`, `409`, `422`); DTOs REST em JSON; OpenAPI local.

## 🛠️ Stack Tecnológica

- **Java 25**
- **Spring Boot 4** (Spring MVC)
- **PostgreSQL** (persistência, um banco por serviço)
- **Flyway** (migrations; schema é a fonte da verdade, `ddl-auto: validate`)
- **springdoc-openapi** (OpenAPI/Swagger local)
- **Testcontainers**, **JUnit 5**, **ArchUnit** (testes)

## 📁 Estrutura do Projeto

O serviço segue **Arquitetura Hexagonal (Ports & Adapters)**:

```
src/main/java/com/mercatus/catalog/
├── domain/                     # núcleo: regras e modelo, livre de frameworks
│   ├── shared/                 # Money, Slug, Status, PageQuery, PageResult
│   ├── category/               # Category (agregado)
│   ├── product/                # Product, Sku, SkuPriceHistory, ProductFilter
│   └── exception/              # exceções de regra/estado de domínio
├── application/                # casos de uso + portas
│   ├── category/
│   │   ├── port/in/            # *UseCase (portas de entrada) + commands
│   │   ├── port/out/           # CategoryRepository (porta de saída)
│   │   └── usecase/            # implementações (uma classe por caso de uso) + support/
│   ├── product/
│   │   ├── port/in/            # 12 *UseCase + commands
│   │   ├── port/out/           # ProductRepository, SkuPriceHistoryRepository
│   │   └── usecase/            # implementações + support/ (ProductGateway)
│   └── exception/              # BusinessRule/ResourceNotFound/StateConflict (422/404/409)
├── infrastructure/             # adapters de saída (driven)
│   └── persistence/{category,product,support}   # entities JPA, JpaRepositories, mappers, adapters
└── web/                        # adapters de entrada (driving)
    ├── category/               # CategoryController + dto/
    ├── product/                # ProductController + dto/
    ├── common/                 # PageResponse, PingController
    ├── error/                  # ApiExceptionHandler (Problem Details)
    └── config/                 # CorrelationIdFilter, OpenApiConfig
```

Fluxo hexagonal: o `web` (adapter de entrada) depende das **portas de entrada**
(`application/.../port/in/*UseCase`); as implementações em `usecase/` orquestram o
domínio e dependem das **portas de saída** (`application/.../port/out/*Repository`),
implementadas pelos adapters em `infrastructure/persistence`. O domínio não conhece
nenhuma borda.

A arquitetura é verificada por ArchUnit: `domain` não depende de nenhuma outra
camada nem de frameworks (Spring/JPA/validation/Jackson); `application` só depende
de `domain` e de si mesma; as portas de saída só são implementadas por
`infrastructure`; `@Entity` só vive em `infrastructure` e `@RestController` só em `web`.

## ⚙️ Configuração

### Pré-requisitos

- Java 25
- Maven 3.9+
- Docker (Testcontainers e ambiente local)
- PostgreSQL 17 (fornecido pelo `mercatus-platform` ou por Testcontainers nos testes)
- Acesso ao GitHub Packages para consumir o `mercatus-parent`
  (variáveis `MAVEN_USERNAME` e `MAVEN_PASSWORD`, ver `.mvn/settings.xml`)

### Variáveis de Ambiente

| Variável              | Descrição                       | Valor Padrão                               |
|-----------------------|---------------------------------|--------------------------------------------|
| `CATALOG_DB_URL`      | URL JDBC do banco do catálogo   | `jdbc:postgresql://localhost:5432/catalog` |
| `CATALOG_DB_USER`     | Usuário do banco                | `mercatus`                                 |
| `CATALOG_DB_PASSWORD` | Senha do banco                  | `mercatus`                                 |
| `MAVEN_USERNAME`      | Usuário do GitHub Packages      | -                                          |
| `MAVEN_PASSWORD`      | Token do GitHub Packages        | -                                          |

### Rodar Localmente

```bash
# 1. Subir as dependências locais (Postgres) via mercatus-platform (perfil core)
#    no repositório mercatus-platform:
#    docker compose --profile core up -d

# 2. Rodar a aplicação (Flyway aplica as migrations no start)
./mvnw spring-boot:run

# 3. Acessar o OpenAPI/Swagger local
open http://localhost:8080/api/v1/swagger-ui.html
```

## 📚 Documentação

- **Swagger UI:** http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api/v1/v3/api-docs
- **Health Check:** http://localhost:8080/api/v1/actuator/health

### Endpoints principais (prefixo `/api/v1`)

- `POST /categories`, `GET /categories/{id}`, `GET /categories`, `PUT /categories/{id}`
- `POST /products`, `GET /products/{id}`, `GET /products`, `PUT /products/{id}`
- `POST /products/{id}:activate`, `POST /products/{id}:deactivate`
- `POST /products/{id}/skus`, `PUT /products/{id}/skus/{skuId}`
- `PUT /products/{id}/skus/{skuId}/price`, `GET /products/{id}/skus/{skuId}/price-history`
- `POST /products/{id}/skus/{skuId}:activate`, `POST /products/{id}/skus/{skuId}:deactivate`

## 📝 Observações

### Modelagem

- `Product` é a raiz de agregado e contém seus SKUs; a consistência de ativação é do agregado.
- `Category` é um agregado separado, referenciado por id (sem navegação de objeto entre agregados).
- `SkuPriceHistory` é imutável: toda alteração de preço grava uma linha; nada é apagado.

### Regras de estado e preço

- Ativar um `Product` exige categoria **e** ao menos um SKU com preço, senão `422`.
- Um SKU está disponível para venda quando o SKU e o seu `Product` estão `ACTIVE`.
- Preço é BRL, 2 casas, nunca negativo (`422` se negativo).

### Concorrência

- Cada tabela tem coluna `version` para optimistic locking; conflito de versão retorna `409`.
- As atualizações exigem o campo `version` no corpo da requisição.

## 🧪 Testes

```bash
# Rodar os testes unitários (surefire)
./mvnw test

# Rodar unitários + integração (failsafe, usa Testcontainers) e empacotar
./mvnw verify
```

- Unitários de domínio: `Money`, `Slug`, regra de ativação, disponibilidade de SKU, histórico de preço.
- Integração (Testcontainers Postgres): cenários de ativação, unicidade de slug/code,
  conflito de versão → 409, filtros + paginação, histórico de preço.
- ArchUnit: regras de camadas.

## 📄 Licença

Projeto de estudo da plataforma Mercatus — uso interno.
