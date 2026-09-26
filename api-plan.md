## API Plan

### Authentication
- `POST /auth/register` — Register customer
- `POST /auth/login` — Login and receive JWT

### Customer / Accounts
- `POST /accounts` — Create own account
- `GET /accounts` — View own accounts
- `GET /accounts/{id}` — View own account details
- `PATCH /accounts/{id}/close` — Close/deactivate own account if allowed

### Transfers
- `POST /transfers` — Transfer money between accounts
- `GET /transfers/{id}` — View own transfer details

### Transactions
- `GET /transactions` — View own transaction history
- `GET /transactions/{id}` — View own transaction details
- `POST /accounts/{id}/withdrawals` — Withdraw money from own account

### Staff
- `POST /accounts/{id}/deposits` — Deposit money into an account
- `GET /staff/users` — View users
- `GET /staff/accounts` — View all accounts
- `GET /staff/accounts/{id}` — View account details
- `PATCH /staff/accounts/{id}/status` — Block, unblock or mark account for review
- `GET /staff/transfers` — View transfers
- `GET /staff/transactions` — View transactions

### Admin
- `POST /admin/staff` — Create staff user
- `PATCH /admin/users/{id}/role` — Change user role
- `PATCH /admin/staff/{id}/status` — Activate/deactivate staff
- `GET /admin/audit-logs` — View audit log