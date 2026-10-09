CREATE TABLE transaction
(

    id            BIGSERIAL primary key,

    type          VARCHAR(15)        NOT NULL,
    amount        NUMERIC(19, 2)     NOT NULL,
    balance_after NUMERIC(19, 2)     NOT NULL,

    reference     VARCHAR(30)        NOT NULL,

    created_at    TIMESTAMP          NOT NULL,

    account_id    BIGINT             NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account (id),

    transfer_id   BIGINT             NOT NULL,
    FOREIGN KEY (transfer_id) REFERENCES transfers (id)
)