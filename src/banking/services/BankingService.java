package banking.services;

import banking.data.DataManager;
import banking.models.*;
import java.util.List;

public class BankingService {
    private final DataManager dm;

    public BankingService() {
        this.dm = DataManager.getInstance();
    }

    // ========== ACCOUNT OPERATIONS ==========

    public Account createAccount(String userId, Account.AccountType type, double initialDeposit) {
        if (initialDeposit < 0) return null;
        Account account = new Account(userId, type, initialDeposit);
        dm.addAccount(account);
        // Record opening transaction
        Transaction tx = new Transaction(account.getId(), userId,
                Transaction.TransactionType.ACCOUNT_OPENED, initialDeposit,
                initialDeposit, "Account Opened with initial deposit", null);
        dm.addTransaction(tx);
        return account;
    }

    public boolean closeAccount(String accountId, String userId) {
        Account account = dm.findAccountById(accountId).orElse(null);
        if (account == null || !account.getUserId().equals(userId)) return false;
        dm.closeAccount(accountId);
        return true;
    }

    // ========== TRANSACTION OPERATIONS ==========

    public boolean deposit(String accountId, String userId, double amount, String note) {
        if (amount <= 0) return false;
        Account account = dm.findAccountById(accountId).orElse(null);
        if (account == null || !account.isActive()) return false;

        account.deposit(amount);
        dm.updateAccount(account);

        String desc = note.isBlank() ? "Cash Deposit" : note;
        Transaction tx = new Transaction(accountId, userId,
                Transaction.TransactionType.DEPOSIT, amount, account.getBalance(), desc, null);
        dm.addTransaction(tx);
        return true;
    }

    public boolean withdraw(String accountId, String userId, double amount, String note) {
        if (amount <= 0) return false;
        Account account = dm.findAccountById(accountId).orElse(null);
        if (account == null || !account.isActive()) return false;
        if (!account.withdraw(amount)) return false;

        dm.updateAccount(account);
        String desc = note.isBlank() ? "Cash Withdrawal" : note;
        Transaction tx = new Transaction(accountId, userId,
                Transaction.TransactionType.WITHDRAWAL, amount, account.getBalance(), desc, null);
        dm.addTransaction(tx);
        return true;
    }

    public String transfer(String fromAccountId, String toAccountNumber, String userId,
                           double amount, String note) {
        if (amount <= 0) return "Amount must be greater than zero.";

        Account from = dm.findAccountById(fromAccountId).orElse(null);
        if (from == null || !from.isActive()) return "Source account not found or inactive.";

        Account to = dm.findAccountByNumber(toAccountNumber).orElse(null);
        if (to == null || !to.isActive()) return "Destination account not found or inactive.";
        if (from.getId().equals(to.getId())) return "Cannot transfer to the same account.";

        if (!from.withdraw(amount)) return "Insufficient balance.";
        to.deposit(amount);

        dm.updateAccount(from);
        dm.updateAccount(to);

        String desc = note.isBlank() ? "Funds Transfer" : note;

        // Out transaction for sender
        Transaction txOut = new Transaction(from.getId(), userId,
                Transaction.TransactionType.TRANSFER_OUT, amount, from.getBalance(),
                "Transfer to " + to.getAccountNumber() + ": " + desc, to.getId());
        dm.addTransaction(txOut);

        // In transaction for receiver
        Transaction txIn = new Transaction(to.getId(), to.getUserId(),
                Transaction.TransactionType.TRANSFER_IN, amount, to.getBalance(),
                "Transfer from " + from.getAccountNumber() + ": " + desc, from.getId());
        dm.addTransaction(txIn);

        return null; // null = success
    }

    // ========== QUERY OPERATIONS ==========

    public List<Account> getUserAccounts(String userId) {
        return dm.getAccountsByUser(userId);
    }

    public List<Transaction> getAccountTransactions(String accountId) {
        return dm.getTransactionsByAccount(accountId);
    }

    public List<Transaction> getUserTransactions(String userId) {
        return dm.getTransactionsByUser(userId);
    }

    public List<Account> getAllAccounts() {
        return dm.getAllAccounts();
    }

    public List<Transaction> getAllTransactions() {
        return dm.getAllTransactions();
    }

    public double getTotalDeposits() {
        return dm.getAllAccounts().stream()
                .filter(Account::isActive)
                .mapToDouble(Account::getBalance).sum();
    }

    public long getActiveAccountCount() {
        return dm.getAllAccounts().stream().filter(Account::isActive).count();
    }
}
