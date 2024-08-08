CREATE TABLE outbox_message
(
    id         UUID      NOT NULL PRIMARY KEY,
    topic      TEXT      NOT NULL,
    type       TEXT      NOT NULL,
    payload    BYTEA     NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
