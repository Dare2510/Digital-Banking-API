CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_profile
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    name         VARCHAR(50)  NOT NULL,
    surname      VARCHAR(50)  NOT NULL,
    street       VARCHAR(100) NOT NULL,
    house_number VARCHAR(10)  NOT NULL,
    city         VARCHAR(100) NOT NULL,
    zip_code     VARCHAR(15)  NOT NULL,
    country      VARCHAR(100) NOT NULL,
    status       VARCHAR(10)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,

    CONSTRAINT uk_user_profile_user UNIQUE (user_id),
    CONSTRAINT fk_user_profile_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);