CREATE TABLE policy_ref
(
    policy          UUID NOT NULL,
    instalment_list UUID NOT NULL,

    PRIMARY KEY (policy, instalment_list)
);

CREATE TABLE instalment
(
    id                  UUID PRIMARY KEY,
    installment_list_id UUID           NOT NULL,
    amount              DECIMAL(10, 2) NOT NULL,
    due                 DATE           NOT NULL,
    is_paid             BOOLEAN        NOT NULL,
    seq_index           INTEGER        NOT NULL
);
