# BankPro API

This module exposes the BankPro core services as a RESTful HTTP API using Spring Boot.

## Base URL
When running locally: `http://localhost:8080`

## Endpoints

### Authentication
- `POST /auth/login` - Authenticate and get JWT token (requires `username`, `password`)
- `POST /auth/register` - Register a new customer

*All subsequent endpoints require the JWT token to be passed in the `Authorization` header as a Bearer token: `Authorization: Bearer <token>`*

### Accounts
- `GET /accounts/{userId}` - List all accounts for a user
- `POST /accounts/{accountId}/deposit` - Deposit funds (`amount`, `description`)
- `POST /accounts/{accountId}/withdraw` - Withdraw funds (`amount`, `description`)
- `GET /accounts/{accountId}/statement?from=YYYY-MM-DD&to=YYYY-MM-DD` - Download account statement as PDF

### Transfers
- `POST /transfer` - Transfer funds between accounts (`fromAccountId`, `toAccountNumber`, `amount`, `description`)

### Transactions
- `GET /transactions/{userId}` - Get transaction history for a user

### Loans
- `GET /loans/{userId}` - List all loans for a user
- `POST /loans/apply` - Apply for a new loan (`accountId`, `type`, `amount`, `tenureMonths`, `purpose`)
- `POST /loans/{loanId}/repay` - Repay a loan (`accountId`, `amount`)
