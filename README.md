# Digital Banking API

A Spring Boot backend application that simulates core digital banking operations.

The goal of this project is to build a transactional banking system with a strong focus on data consistency,
concurrency, security and database integrity.

## Planned Core Features

- User registration and authentication with JWT
- Bank account management
- Deposits and withdrawals
- Money transfers between accounts
- Transaction history
- Account ownership and authorization
- Prevention of negative balances
- Transactional rollback on failed transfers
- Optimistic locking for concurrent account updates
- Pagination, filtering and sorting of transactions
- Database migrations with Flyway
- PostgreSQL integration
- Integration and concurrency testing with Testcontainers

## Planned Technical Focus

- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- JWT Authentication
- Bean Validation
- Docker / Docker Compose
- Testcontainers
- JUnit / Mockito / MockMvc

## Core Business Rules

- An account belongs to exactly one user.
- Each account uses exactly one currency.
- Transfers are only allowed between accounts with the same currency.
- An account balance must never become negative.
- A transfer must either complete fully or be rolled back completely.
- Users may only access and manage their own accounts.