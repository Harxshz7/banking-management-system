CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT NOT NULL,
    address TEXT,
    role TEXT NOT NULL,
    credit_score INTEGER NOT NULL,
    transaction_pin TEXT,
    otp_code TEXT,
    otp_expiry TEXT,
    created_at TEXT NOT NULL,
    active INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id TEXT PRIMARY KEY,
    account_number TEXT UNIQUE NOT NULL,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    balance REAL NOT NULL,
    minimum_balance REAL NOT NULL,
    interest_rate REAL NOT NULL,
    daily_withdrawal_limit REAL NOT NULL,
    today_withdrawn REAL NOT NULL,
    last_reset_date TEXT,
    created_at TEXT,
    active INTEGER NOT NULL,
    frozen INTEGER NOT NULL,
    description TEXT,
    fd_maturity_date TEXT,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    id TEXT PRIMARY KEY,
    receipt_number TEXT UNIQUE NOT NULL,
    account_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    balance_after REAL NOT NULL,
    description TEXT,
    related_account_id TEXT,
    timestamp TEXT NOT NULL,
    channel TEXT NOT NULL,
    FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS loans (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    credit_account_id TEXT NOT NULL,
    type TEXT NOT NULL,
    principal_amount REAL NOT NULL,
    interest_rate REAL NOT NULL,
    tenure_months INTEGER NOT NULL,
    emi_amount REAL NOT NULL,
    total_payable REAL NOT NULL,
    amount_paid REAL NOT NULL,
    status TEXT NOT NULL,
    purpose TEXT,
    applied_at TEXT NOT NULL,
    processed_at TEXT,
    admin_note TEXT,
    months_paid INTEGER NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(credit_account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS beneficiaries (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    account_number TEXT NOT NULL,
    nickname TEXT,
    bank_note TEXT,
    added_at TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT NOT NULL,
    created_at TEXT NOT NULL,
    read INTEGER NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    action TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recurring_deposits (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    account_id TEXT NOT NULL,
    monthly_amount REAL NOT NULL,
    months INTEGER NOT NULL,
    interest_rate REAL NOT NULL,
    start_date TEXT NOT NULL,
    maturity_date TEXT NOT NULL,
    accumulated_amount REAL NOT NULL,
    active INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS scheduled_transfers (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    from_account_id TEXT NOT NULL,
    to_account_number TEXT NOT NULL,
    amount REAL NOT NULL,
    description TEXT,
    scheduled_date TEXT NOT NULL,
    recurring_monthly INTEGER NOT NULL,
    executed INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(from_account_id) REFERENCES accounts(id) ON DELETE CASCADE
);
