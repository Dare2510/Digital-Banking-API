# Digital Banking API — API Plan

Base path: `/api`

## Authentication / Registration

- POST /customer/auth/register — Register customer
- POST /customer/auth/register/profile — Complete customer profile
- POST /auth/login — Login and receive JWT

## Customer / Accounts

- POST /customer/accounts — Create account
  GET /customer/accounts/all - Get all accounts by user
- GET /customer/accounts/{accountId} — Get account details
- POST /customer/accounts/withdrawals — Withdraw money
- POST /customer/accounts/{accountId}/close — Close account

## Transfers

- POST /customer/transfers — Create transfer
- GET /customer/transfers/{transferId} — Get transfer details
- GET /customer/transfers/all/{accountId} — Get outgoing and incoming transfers

## Staff

- POST /staff/accounts/deposits — Deposit money
- PATCH /staff/accounts/{accountId}/status — Block or unblock account
- GET /staff/users — List users (paginated)
- GET /staff/accounts — List accounts (paginated)
- GET /staff/accounts/{accountId} — Get account details
- GET /staff/transfers — List transfers (paginated)
- GET /staff/transactions — List transactions (paginated)

## Admin

- POST /admin/employee — Create employee
- POST /admin/employee/{userId}/profile — Create employee profile
- PATCH /admin/users/{userId}/userRole — Change user userRole

## Planned Features

- GET /transactions — Get authenticated user's transactions
- GET /transactions/{transactionId} — Get transaction details
- Account ownership validation for customer operations
- Role-based access control (CUSTOMER, STAFF, ADMIN)
- Spring Security / JWT authorization
- Audit logging (optional)

## Account Status Rules

- ACTIVE → BLOCKED: allowed
- BLOCKED → ACTIVE: allowed
- ACTIVE → CLOSED: via close-account operation only
- CLOSED → ACTIVE/BLOCKED: forbidden
- BLOCKED/CLOSED accounts cannot perform monetary operations
- Closing an account requires ACTIVE status and zero balance
