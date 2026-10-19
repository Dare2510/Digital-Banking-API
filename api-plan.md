# Digital Banking API — API Plan

Base path: `/api`

## Authentication / Registration

- POST /auth/register — Register customer
- POST /auth/register/profile — Complete customer profile
- POST /auth/login — Login and receive JWT [PLANNED]

## Customer / Accounts

- POST /accounts — Create account
  GET /accounts/all/{userId} — Temporary endpoint; to be replaced after JWT authentication.
- GET /accounts/{accountId} — Get account details
- POST /accounts/withdrawals — Withdraw money
- POST /accounts/{accountId}/close — Close account

## Transfers

- POST /transfers — Create transfer
- GET /transfers/{transferId} — Get transfer details
- GET /transfers/all/{accountId} — Get outgoing and incoming transfers

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
- POST /admin/employee/profile — Create employee profile
- PATCH /admin/users/{userId}/userRole — Change user userRole

## Planned Features

- GET /accounts — Get authenticated user's accounts
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
