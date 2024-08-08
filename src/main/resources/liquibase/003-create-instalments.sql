CREATE TABLE instalment
(
    id                  UUID PRIMARY KEY,
    installment_list_id UUID           NOT NULL,
    amount              DECIMAL(10, 2) NOT NULL,
    due                 DATE           NOT NULL,
    is_paid             BOOLEAN        NOT NULL,
    seq_index           INTEGER        NOT NULL
);

CREATE TABLE installment_list
(
    id         UUID PRIMARY KEY,
    saldo      DECIMAL(10, 2) NOT NULL,
    version    BIGINT         NOT NULL
);

CREATE TABLE policy_ref
(
    installment_list UUID NOT NULL REFERENCES installment_list (id),
    policy           UUID NOT NULL,

    PRIMARY KEY (installment_list, policy)
);
