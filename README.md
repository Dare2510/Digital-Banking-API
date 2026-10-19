# Digital Banking API

A Spring Boot backend application that simulates core digital banking operations.

The goal of this project is to build a transactional banking system with a strong focus on data consistency,
concurrency, security, and database integrity.

## Implemented Features

- Customer and employee registration
- User profile management
- Bank account creation and closure
- Account blocking and unblocking
- Deposits and withdrawals
- Money transfers between accounts
- Transaction and transfer history
- Prevention of negative account balances
- Transactional rollback on failed transfers
- Optimistic locking for concurrent account updates
- Bean Validation for incoming requests
- Pagination and sorting for administrative operations
- Database migrations with Flyway
- PostgreSQL integration
- Docker Compose configuration

## Planned Features

- Spring Security and JWT authentication
- Role-based authorization (CUSTOMER, STAFF, ADMIN)
- Account ownership enforcement
- Customer transaction history endpoints
- Integration and concurrency testing with Testcontainers
- Audit logging (optional)

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Bean Validation
- Docker / Docker Compose
- Lombok
- ModelMapper
- JUnit / Mockito / MockMvc
- Testcontainers

## Core Business Rules

### User Management

- Users can register with an email address and password.
- Each user can have one profile.
- A completed user profile is required before creating a bank account.
- User roles are CUSTOMER, STAFF, and ADMIN.

### Bank Accounts

- An account belongs to exactly one user.
- Each account uses exactly one currency (EUR, USD, JPY).
- Account statuses are ACTIVE, BLOCKED, and CLOSED.
- Only ACTIVE accounts can perform monetary operations.
- ACTIVE accounts can be blocked and BLOCKED accounts can be reactivated.
- CLOSED accounts cannot be reopened.
- An account can only be closed if it is ACTIVE and has a zero balance.

### Transactions

- Deposits increase the account balance.
- Withdrawals decrease the account balance.
- Account balances must never become negative.
- Each monetary operation creates a transaction ledger entry.
- Each ledger entry stores the account balance after the operation.

### Transfers

- Transfers are only allowed between different accounts.
- Both accounts must be ACTIVE.
- Transfers require matching account currencies.
- The source account must have sufficient funds.
- Each successful transfer creates two ledger entries: TRANSFER_OUT and TRANSFER_IN.
- A transfer must either complete fully or be rolled back completely.

### Security (Planned)

- JWT-based authentication.
- Role-based authorization for CUSTOMER, STAFF, and ADMIN.
- Customers may only access and manage their own accounts.

## Project Documentation

The `docs/` directory contains:

- UML class diagram
- Entity relationship diagram
- Use-case diagram
- Deposit sequence diagram
- Transfer sequence diagram
- Planned JWT login sequence diagram

## Development Status

The core banking functionality is under development. Authentication, authorization,
and additional integration and concurrency testing
are planned before the project is considered complete.