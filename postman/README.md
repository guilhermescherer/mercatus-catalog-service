# Postman — Catalog Service

Arquivos para importar no Postman e testar a API localmente.

- `mercatus-catalog.postman_collection.json` — coleção com todos os endpoints.
- `mercatus-catalog.postman_environment.json` — environment `local` (baseUrl e variáveis).

## Como usar

1. Suba a aplicação (`./mvnw spring-boot:run`) com o Postgres disponível. Base: `http://localhost:8080/api/v1`.
2. No Postman: **Import** → selecione os dois arquivos.
3. Selecione o environment **Mercatus - Catalog (local)** no canto superior direito.
4. Rode na ordem das pastas:
   - **Categorias → Criar categoria** (salva `categoryId`)
   - **Produtos → Criar produto** (salva `productId`)
   - **SKUs → Adicionar SKU (com preço)** (salva `skuId`)
   - **SKUs → Ativar SKU** e **Produtos → Ativar produto**
   - **SKUs → Alterar preço** e **Histórico de preço**

Os requests de criação/alteração capturam automaticamente `id` e `version` nas
variáveis da coleção, então não é preciso copiar valores manualmente. A pasta
**Erros** demonstra as respostas `422`, `409` e `404`.

> Observação: os endpoints de ativar/inativar usam verbo customizado com `:`
> (ex.: `/products/{id}:activate`). Isso é intencional e já está nas URLs.
