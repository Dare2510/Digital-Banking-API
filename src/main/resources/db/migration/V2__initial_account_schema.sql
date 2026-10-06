CREATE TABLE account
(
    id             BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),

    account_number VARCHAR(50) UNIQUE NOT NULL,
    CONSTRAINT UK_account_number UNIQUE (account_number),

    balance        NUMERIC(19, 2)     NOT NULL,
    currency       VARCHAR(3),
    status         VARCHAR            NOT NULL,
    version        BIGINT             NOT NULL DEFAULT 0,
    created_at     TIMESTAMP          NOT NULL
);