# 🏦 BankPro — Banking Management System

[![Build & Test](https://github.com/YOUR_USERNAME/banking-management-system/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/banking-management-system/actions/workflows/build.yml)

> A premium Java Swing desktop banking application with modern banking UI, persistent storage, and full banking features.

---

## 🚀 Quick Start

### Build
```bash
mvn clean package
```

### Run
```bash
mvn exec:java -Dexec.mainClass="banking.Main"
```
Or run the jar directly:
```bash
java -jar target/bankpro-1.0.0.jar
```

### Test
```bash
mvn test
```

---

## 🔐 Default Login Credentials

| Role     | Username | Password  |
|----------|----------|-----------|
| Admin    | admin    | admin123  |
| Customer | john     | john123   |

> Passwords are stored as BCrypt hashes. On first login, legacy plaintext passwords are automatically re-hashed.

---

## ✨ Features

### Customer Portal
- 🏠 **Dashboard** — Total balance overview, account summary cards, and quick insights
- 💳 **My Accounts** — Create Savings/Checking/Fixed Deposit accounts, close accounts, and view account details
- 💰 **Deposit** — Deposit funds into any account with optional notes
- 🏧 **Withdraw** — Withdraw funds with balance validation
- ↔️ **Transfer** — Transfer money to any account by account number
- 📋 **History** — Full color-coded transaction history with timestamps

### Admin Portal
- 📊 **Overview** — System-wide stats (total assets, users, accounts, transactions)
- 👥 **Manage Users** — Add/delete customers, view all user accounts
- 💳 **All Accounts** — View every account across all customers
- 📋 **All Transactions** — Full audit log of every transaction

---

## 🗂️ Project Structure

```
src/
├── main/java/banking/
│   ├── Main.java                          ← Entry point
│   ├── models/
│   │   ├── User.java                      ← User entity
│   │   ├── Account.java                   ← Bank account entity
│   │   ├── Transaction.java               ← Transaction record
│   │   ├── Loan.java                      ← Loan entity
│   │   ├── Beneficiary.java               ← Saved beneficiary
│   │   ├── Notification.java              ← User notification
│   │   ├── RecurringDeposit.java          ← Recurring deposit
│   │   ├── ScheduledTransfer.java         ← Scheduled transfer
│   │   └── AuditLog.java                  ← Audit log entry
│   ├── data/
│   │   └── DataManager.java               ← File-based persistence (serialization)
│   ├── services/
│   │   ├── AuthService.java               ← Login / Register logic
│   │   ├── BankingService.java            ← Deposit / Withdraw / Transfer
│   │   ├── BankingServiceExtensions.java  ← Scheduled transfers, recurring deposits
│   │   └── LoanService.java               ← Loan operations
│   ├── util/
│   │   └── PasswordUtil.java              ← BCrypt hashing utility
│   └── ui/
│       ├── Theme.java                     ← Color palette, fonts, helpers
│       ├── LoginFrame.java                ← Login + Register screen
│       ├── CustomerDashboard.java         ← Customer main window
│       ├── AdminDashboard.java            ← Admin control panel
│       ├── PinDialog.java                 ← Transaction PIN dialog
│       ├── ReceiptDialog.java             ← Transaction receipt
│       └── components/
│           ├── CardPanel.java             ← Rounded card with shadow
│           ├── GradientButton.java        ← Animated gradient button
│           ├── StyledTextField.java       ← Custom text field with placeholder
│           └── StyledPasswordField.java   ← Custom password field
└── test/java/banking/
    └── (JUnit 5 tests)
```

---

## 💾 Data Persistence

All data is stored in `banking_data/` (auto-created on first run):
- `users.dat` — serialized user list
- `accounts.dat` — serialized account list
- `transactions.dat` — serialized transaction list

Data persists across sessions automatically.

---

## 🎨 Tech Stack
- **Language**: Java 17+
- **Build**: Maven
- **UI**: Java Swing (custom-rendered components)
- **Storage**: Java Object Serialization
- **Security**: BCrypt password/PIN hashing (jbcrypt)
- **Testing**: JUnit 5
- **Design**: Dark theme, gradient buttons, glassmorphism cards

---

## 📷 Screenshots
> Replace these placeholders with actual screenshots from the refreshed app.

- `screenshots/login-screen.png`
- `screenshots/customer-dashboard.png`
- `screenshots/admin-dashboard.png`
