CREATE TABLE account (
                         id BIGSERIAL PRIMARY KEY,
                         account_number VARCHAR(50) UNIQUE NOT NULL,
                         balance NUMERIC(19,2) NOT NULL,
                         version BIGINT NOT NULL
);