CREATE TABLE schema_bootstrap (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO schema_bootstrap (description) VALUES ('catalog schema baseline');
