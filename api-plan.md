# Digital Banking API — API Plan

Base path: `/api`

## Authentication / Registration

- POST /auth/register — Register customer
- POST /auth/register/profile — Complete customer profile
- POST /auth/login — Login and receive JWT

## Customer / Accounts

- POST /customer/accounts — Create account
- GET /customer/accounts/all - Authenticated user can get information about all of his accounts
- GET /customer/accounts/{accountId} — Get account details - user needs to be authenticated
- POST /customer/accounts/withdrawals — Withdraw money - user needs to be authenticated
- POST /customer/accounts/{accountId}/close — Close account - user needs to be authenticated

## Transfers

- POST /customer/transfers — Create transfer
- GET /customer/transfers/{transferId} — Get transfer details
- GET /customer/transfers/all/{accountId} — Get outgoing and incoming transfers

## Transactions

-GET /customer/transactions - Get authenticated user's transactions
-GET /customer/transactions/{transactionId} - Get transaction details

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

- Audit logging (optional)

## Account Status Rules

- ACTIVE → BLOCKED: allowed
- BLOCKED → ACTIVE: allowed
- ACTIVE → CLOSED: via close-account operation only
- CLOSED → ACTIVE/BLOCKED: forbidden
- BLOCKED/CLOSED accounts cannot perform monetary operations
- Closing an account requires ACTIVE status and zero balance
