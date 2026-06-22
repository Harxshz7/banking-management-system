package banking.services;

import banking.data.DataManager;
import banking.models.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BankingService {
    private final DataManager dm;

    public BankingService() {
        this.dm = DataManager.getInstance();
    }

    // ====================== ACCOUNT OPERATIONS ======================

    public Account createAccount(String userId, Account.AccountType type, double initialDeposit) {
        if (initialDeposit < 0) return null;
        Account account = new Account(userId, type, initialDeposit);
        dm.addAccount(account);
        Transaction tx = new Transaction(account.getId(), userId,
                Transaction.TransactionType.ACCOUNT_OPENED, initialDeposit,
                initialDeposit, "Account Opened with initial deposit", null);
        dm.addTransaction(tx);
        return account;
    }

    public boolean closeAccount(String accountId, String userId) {
        Account acc = dm.findAccountById(accountId).orElse(null);
        if (acc == null || !acc.getUserId().equals(userId)) return false;
        dm.closeAccount(accountId);
        return true;
    }

    // ====================== DEPOSIT ======================

    public TransactionResult deposit(String accountId, String userId, double amount,
                                     String description, String channel) {
        if (amount <= 0) return TransactionResult.fail("Amount must be greater than zero.");
        if (amount > 1_000_000) return TransactionResult.fail("Deposit cannot exceed $1,000,000 at once.");

        Account account = dm.findAccountById(accountId).orElse(null);
        if (account == null || !account.isActive()) return TransactionResult.fail("Account not found or inactive.");
        if (account.isFrozen()) return TransactionResult.fail("Account is frozen. Contact the bank.");

        account.deposit(amount);
        dm.updateAccount(account);

        String desc = (description == null || description.isBlank()) ? "Cash Deposit" : description;
        Transaction tx = new Transaction(accountId, userId,
                Transaction.TransactionType.DEPOSIT, amount, account.getBalance(), desc, null);
        tx.setChannel(channel != null ? channel : "ONLINE");
        dm.addTransaction(tx);

        return TransactionResult.success(tx, account);
    }

    // ====================== WITHDRAWAL ======================

    public TransactionResult withdraw(String accountId, String userId, double amount,
                                      String description, String channel) {
        if (amount <= 0) return TransactionResult.fail("Amount must be greater than zero.");

        Account account = dm.findAccountById(accountId).orElse(null);
        if (account == null || !account.isActive()) return TransactionResult.fail("Account not found or inactive.");

        // Delegates to Account's tryWithdraw for all business rule checks
        String error = account.tryWithdraw(amount);
        if (error != null) return TransactionResult.fail(error);

        dm.updateAccount(account);

        String desc = (description == null || description.isBlank()) ? "Cash Withdrawal" : description;
        Transaction tx = new Transaction(accountId, userId,
                Transaction.TransactionType.WITHDRAWAL, amount, account.getBalance(), desc, null);
        tx.setChannel(channel != null ? channel : "ONLINE");
        dm.addTransaction(tx);

        return TransactionResult.success(tx, account);
    }

    // ====================== TRANSFER ======================

    public TransactionResult transfer(String fromAccountId, String toAccountNumber,
                                       String userId, double amount,
                                       String description, boolean saveBeneficiary,
                                       String beneficiaryName, String beneficiaryNick) {
        if (amount <= 0) return TransactionResult.fail("Amount must be greater than zero.");

        Account from = dm.findAccountById(fromAccountId).orElse(null);
        if (from == null || !from.isActive()) return TransactionResult.fail("Source account not found or inactive.");

        Account to = dm.findAccountByNumber(toAccountNumber).orElse(null);
        if (to == null || !to.isActive()) return TransactionResult.fail("Destination account not found or inactive.");
        if (from.getId().equals(to.getId())) return TransactionResult.fail("Cannot transfer to the same account.");
        if (to.isFrozen()) return TransactionResult.fail("Destination account is frozen.");

        // Use tryWithdraw for all business rule validation
        String error = from.tryWithdraw(amount);
        if (error != null) return TransactionResult.fail(error);

        to.deposit(amount);
        dm.updateAccount(from);
        dm.updateAccount(to);

        String desc = (description == null || description.isBlank()) ? "Funds Transfer" : description;

        Transaction txOut = new Transaction(from.getId(), userId,
                Transaction.TransactionType.TRANSFER_OUT, amount, from.getBalance(),
                "Transfer to " + to.getAccountNumber() + " — " + desc, to.getId());
        dm.addTransaction(txOut);

        Transaction txIn = new Transaction(to.getId(), to.getUserId(),
                Transaction.TransactionType.TRANSFER_IN, amount, to.getBalance(),
                "Transfer from " + from.getAccountNumber() + " — " + desc, from.getId());
        dm.addTransaction(txIn);

        // Save beneficiary if requested
        if (saveBeneficiary && beneficiaryName != null && !beneficiaryName.isBlank()) {
            boolean exists = dm.getBeneficiariesByUser(userId).stream()
                    .anyMatch(b -> b.getAccountNumber().equals(toAccountNumber));
            if (!exists) {
                Beneficiary ben = new Beneficiary(userId, beneficiaryName,
                        toAccountNumber, beneficiaryNick == null ? "" : beneficiaryNick);
                dm.addBeneficiary(ben);
            }
        }

        return TransactionResult.success(txOut, from);
    }

    // ====================== INTEREST ======================

    public TransactionResult applyInterest(String accountId, String userId) {
        Account account = dm.findAccountById(accountId).orElse(null);
        if (account == null || !account.isActive()) return TransactionResult.fail("Account not found.");

        double interest = account.applyMonthlyInterest();
        if (interest <= 0) return TransactionResult.fail("No interest applicable.");

        dm.updateAccount(account);
        Transaction tx = new Transaction(accountId, userId,
                Transaction.TransactionType.INTEREST_CREDIT, interest, account.getBalance(),
                String.format("Monthly Interest @ %.2f%% p.a.", account.getInterestRate()), null);
        dm.addTransaction(tx);
        return TransactionResult.success(tx, account);
    }

    // ====================== QUERY OPERATIONS ======================

    public List<Account> getUserAccounts(String userId) { return dm.getAccountsByUser(userId); }
    public List<Transaction> getAccountTransactions(String accountId) { return dm.getTransactionsByAccount(accountId); }
    public List<Transaction> getUserTransactions(String userId) { return dm.getTransactionsByUser(userId); }
    public List<Account> getAllAccounts() { return dm.getAllAccounts(); }
    public List<Transaction> getAllTransactions() { return dm.getAllTransactions(); }

    public List<Transaction> getFilteredTransactions(String userId,
            LocalDate from, LocalDate to, Transaction.TransactionType typeFilter) {
        return dm.getTransactionsByUser(userId).stream()
                .filter(t -> {
                    LocalDate d = t.getTimestamp().toLocalDate();
                    boolean dateOk = (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to));
                    boolean typeOk = typeFilter == null || t.getType() == typeFilter;
                    return dateOk && typeOk;
                })
                .collect(Collectors.toList());
    }

    public double getTotalAssets() {
        return dm.getAllAccounts().stream().filter(a -> a.isActive() && !a.isFrozen())
                .mapToDouble(Account::getBalance).sum();
    }

    public long getActiveAccountCount() {
        return dm.getAllAccounts().stream().filter(Account::isActive).count();
    }

    // ====================== BENEFICIARY OPERATIONS ======================

    public List<Beneficiary> getBeneficiaries(String userId) { return dm.getBeneficiariesByUser(userId); }
    public void addBeneficiary(Beneficiary b) { dm.addBeneficiary(b); }
    public void removeBeneficiary(String id) { dm.removeBeneficiary(id); }

    // ====================== RESULT WRAPPER ======================

    public static class TransactionResult {
        private final boolean success;
        private final String errorMessage;
        private final Transaction transaction;
        private final Account accountAfter;

        private TransactionResult(boolean success, String errorMessage,
                                   Transaction transaction, Account accountAfter) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.transaction = transaction;
            this.accountAfter = accountAfter;
        }

        public static TransactionResult success(Transaction tx, Account acc) {
            return new TransactionResult(true, null, tx, acc);
        }

        public static TransactionResult fail(String message) {
            return new TransactionResult(false, message, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Transaction getTransaction() { return transaction; }
        public Account getAccountAfter() { return accountAfter; }
    }
}
