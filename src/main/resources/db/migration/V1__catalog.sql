CREATE TABLE category (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL,
    parent_id  UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version    BIGINT NOT NULL,
    CONSTRAINT uq_category_slug UNIQUE (slug),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category (id)
);

CREATE TABLE product (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID,
    attributes  JSONB NOT NULL DEFAULT '{}'::jsonb,
    status      VARCHAR(32) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    version     BIGINT NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE INDEX idx_product_category ON product (category_id);
CREATE INDEX idx_product_status ON product (status);

CREATE TABLE sku (
    id                   UUID PRIMARY KEY,
    product_id           UUID NOT NULL,
    code                 VARCHAR(255) NOT NULL,
    variation_attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    price                NUMERIC(19, 2),
    status               VARCHAR(32) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    version              BIGINT NOT NULL,
    CONSTRAINT uq_sku_code UNIQUE (code),
    CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT ck_sku_price_non_negative CHECK (price IS NULL OR price >= 0)
);

CREATE INDEX idx_sku_product ON sku (product_id);

CREATE TABLE sku_price_history (
    id         UUID PRIMARY KEY,
    sku_id     UUID NOT NULL,
    price      NUMERIC(19, 2) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_price_history_sku FOREIGN KEY (sku_id) REFERENCES sku (id),
    CONSTRAINT ck_price_history_non_negative CHECK (price >= 0)
);

CREATE INDEX idx_price_history_sku_changed_at ON sku_price_history (sku_id, changed_at);
