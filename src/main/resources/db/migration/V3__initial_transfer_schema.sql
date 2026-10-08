CREATE TABLE transfers
(
    id              BIGSERIAL PRIMARY KEY,
    amount          NUMERIC(19, 2) NOT NULL,
    from_account_id BIGINT         NOT NULL,
    FOREIGN KEY (from_account_id) REFERENCES account (id),

    to_account_id   BIGINT         NOT NULL,
    FOREIGN KEY (to_account_id) REFERENCES account (id),

    currency        VARCHAR(3)     NOT NULL,

    status          VARCHAR(10)    NOT NULL,
    created_at      TIMESTAMP      NOT NULL,
    updated_at      TIMESTAMP      NOT NULL


);