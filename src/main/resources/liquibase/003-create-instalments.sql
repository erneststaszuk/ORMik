CREATE TABLE policy_instalments
(
    policy_id UUID PRIMARY KEY,
    version   BIGINT NOT NULL
);

CREATE TABLE instalment
(
    id        UUID PRIMARY KEY,
    policy_id UUID REFERENCES policy_instalments (policy_id),
    amount    DECIMAL(10, 2) NOT NULL,
    due       DATE           NOT NULL,
    is_paid   BOOLEAN        NOT NULL,
    seq_index INTEGER        NOT NULL
);
