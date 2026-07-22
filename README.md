# 🏦 BankPro — Banking Management System
> A premium Java Swing desktop banking application with modern banking UI, persistent storage, and full banking features.

---

## 🚀 Quick Start

### Compile
```bat
compile.bat
```
Or manually:
```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter "*.java" src).FullName
```

### Run
```bat
run.bat
```
Or:
```powershell
java -cp out banking.Main
```

---

## 🔐 Default Login Credentials

| Role     | Username | Password  |
|----------|----------|-----------|
| Admin    | admin    | admin123  |
| Customer | john     | john123   |

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
src/banking/
├── Main.java                          ← Entry point
├── models/
│   ├── User.java                      ← User entity
│   ├── Account.java                   ← Bank account entity
│   └── Transaction.java               ← Transaction record
├── data/
│   └── DataManager.java               ← File-based persistence (serialization)
├── services/
│   ├── AuthService.java               ← Login / Register logic
│   └── BankingService.java            ← Deposit / Withdraw / Transfer
└── ui/
    ├── Theme.java                      ← Color palette, fonts, helpers
    ├── LoginFrame.java                 ← Login + Register screen
    ├── CustomerDashboard.java          ← Customer main window
    ├── AdminDashboard.java             ← Admin control panel
    └── components/
        ├── CardPanel.java              ← Rounded card with shadow
        ├── GradientButton.java         ← Animated gradient button
        ├── StyledTextField.java        ← Custom text field with placeholder
        └── StyledPasswordField.java    ← Custom password field
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
- **UI**: Java Swing (custom-rendered components)
- **Storage**: Java Object Serialization
- **Design**: Dark theme, gradient buttons, glassmorphism cards

---

## ✨ UI/UX Improvements
- Modern navy-to-teal theme with crisp contrast and polished component styling
- Refreshed login screen with floating labels, refined form fields, and improved spacing
- Redesigned customer and admin dashboards for better card layouts, sidebar navigation, and quick insights
- Consistent visual language across buttons, inputs, cards, dialogs, and headers
- Preserved all existing banking business logic, transaction flow, and persistence behavior

## 📷 Screenshots
> Replace these placeholders with actual screenshots from the refreshed app.

- `screenshots/login-screen.png`
- `screenshots/customer-dashboard.png`
- `screenshots/admin-dashboard.png`
