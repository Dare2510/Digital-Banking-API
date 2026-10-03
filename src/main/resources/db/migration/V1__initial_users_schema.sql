CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,

    name          VARCHAR(50)  NOT NULL,
    surname       VARCHAR(50)  NOT NULL,

    email         VARCHAR(255) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),

    street        VARCHAR(100) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    zip_code      VARCHAR(15)  NOT NULL,
    country       VARCHAR(100) NOT NULL,

    password_hash VARCHAR(100) NOT NULL,

    role          VARCHAR(20)  NOT NULL,

    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
