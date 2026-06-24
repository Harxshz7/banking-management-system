package banking.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account implements Serializable {
    private static final long serialVersionUID = 2L;

    public enum AccountType { SAVINGS, CHECKING, FIXED_DEPOSIT }

    private String id;
    private String accountNumber;
    private String userId;
    private AccountType type;
    private double balance;
    private double minimumBalance;
    private double interestRate;          // Annual % e.g. 3.5
    private double dailyWithdrawalLimit;  // Max per day
    private double todayWithdrawn;        // Reset daily
    private LocalDate lastResetDate;
    private LocalDateTime createdAt;
    private boolean active;
    private boolean frozen;
    private String description;
    private LocalDateTime fdMaturityDate; // For FD accounts

    public Account(String userId, AccountType type, double initialBalance) {
        this.id = UUID.randomUUID().toString();
        this.accountNumber = generateAccountNumber();
        this.userId = userId;
        this.type = type;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.frozen = false;
        this.todayWithdrawn = 0;
        this.lastResetDate = LocalDate.now();
        this.description = type.name().replace("_", " ");

        // Set defaults per account type
        switch (type) {
            case SAVINGS -> {
                this.interestRate = 3.5;
                this.minimumBalance = 500.0;
                this.dailyWithdrawalLimit = 5000.0;
            }
            case CHECKING -> {
                this.interestRate = 0.5;
                this.minimumBalance = 0.0;
                this.dailyWithdrawalLimit = 10000.0;
            }
            case FIXED_DEPOSIT -> {
                this.interestRate = 7.0;
                this.minimumBalance = initialBalance; // Locked amount
                this.dailyWithdrawalLimit = 0.0;      // No withdrawals on FD
                this.fdMaturityDate = LocalDateTime.now().plusYears(1);
            }
        }
    }

    private String generateAccountNumber() {
        long num = (long)(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(num);
    }

    // ===== Reset daily limits if new day =====
    private void checkDailyReset() {
        if (lastResetDate == null || !lastResetDate.equals(LocalDate.now())) {
            todayWithdrawn = 0;
            lastResetDate = LocalDate.now();
        }
    }

    // ===== BANKING OPERATIONS =====

    public void deposit(double amount) {
        if (amount > 0) this.balance += amount;
    }

    /** Returns null on success, or error message string */
    public String tryWithdraw(double amount) {
        if (frozen) return "Account is frozen. Please contact the bank.";
        if (type == AccountType.FIXED_DEPOSIT) return "Withdrawals are not allowed on Fixed Deposit accounts.";
        if (amount <= 0) return "Amount must be greater than zero.";
        if (balance - amount < minimumBalance)
            return String.format("Minimum balance of $%,.2f must be maintained. Available: $%,.2f",
                    minimumBalance, balance - minimumBalance);

        checkDailyReset();
        if (dailyWithdrawalLimit > 0 && todayWithdrawn + amount > dailyWithdrawalLimit)
            return String.format("Daily withdrawal limit of $%,.2f exceeded. Remaining today: $%,.2f",
                    dailyWithdrawalLimit, dailyWithdrawalLimit - todayWithdrawn);

        if (balance < amount) return "Insufficient balance.";

        balance -= amount;
        todayWithdrawn += amount;
        return null;
    }

    // Legacy simple withdraw
    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount && !frozen) {
            balance -= amount;
            return true;
        }
        return false;
    }

    // ===== INTEREST =====
    public double calculateMonthlyInterest() {
        return balance * (interestRate / 100.0 / 12.0);
    }

    public double applyMonthlyInterest() {
        double interest = calculateMonthlyInterest();
        balance += interest;
        return interest;
    }

    // ===== GETTERS =====
    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getUserId() { return userId; }
    public AccountType getType() { return type; }
    public double getBalance() { return balance; }
    public double getMinimumBalance() { return minimumBalance; }
    public double getInterestRate() { return interestRate; }
    public double getDailyWithdrawalLimit() { return dailyWithdrawalLimit; }
    public double getAvailableWithdrawal() {
        checkDailyReset();
        return dailyWithdrawalLimit > 0 ? dailyWithdrawalLimit - todayWithdrawn : 0;
    }
    public double getTodayWithdrawn() { checkDailyReset(); return todayWithdrawn; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
    public boolean isFrozen() { return frozen; }
    public String getDescription() { return description; }
    public LocalDateTime getFdMaturityDate() { return fdMaturityDate; }

    // ===== SETTERS =====
    public void setBalance(double balance) { this.balance = balance; }
    public void setActive(boolean active) { this.active = active; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
    public void setDescription(String description) { this.description = description; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public void setMinimumBalance(double minimumBalance) { this.minimumBalance = minimumBalance; }
    public void setDailyWithdrawalLimit(double limit) { this.dailyWithdrawalLimit = limit; }

    // ===== DISPLAY HELPERS =====
    public String getFormattedBalance() { return String.format("$%,.2f", balance); }
    public String getTypeDisplay() { return type.name().replace("_", " "); }

    public String getStatusDisplay() {
        if (!active) return "Closed";
        if (frozen) return "Frozen";
        return "Active";
    }

    @Override
    public String toString() {
        return accountNumber + " - " + getTypeDisplay() + " (" + getFormattedBalance() + ")";
    }
}
