package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum AccountType { SAVINGS, CHECKING, FIXED_DEPOSIT }

    private String id;
    private String accountNumber;
    private String userId;
    private AccountType type;
    private double balance;
    private LocalDateTime createdAt;
    private boolean active;
    private String description;

    public Account(String userId, AccountType type, double initialBalance) {
        this.id = UUID.randomUUID().toString();
        this.accountNumber = generateAccountNumber();
        this.userId = userId;
        this.type = type;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.description = type.name().replace("_", " ");
    }

    private String generateAccountNumber() {
        long num = (long)(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(num);
    }

    // Getters
    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getUserId() { return userId; }
    public AccountType getType() { return type; }
    public double getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
    public String getDescription() { return description; }

    // Setters
    public void setBalance(double balance) { this.balance = balance; }
    public void setActive(boolean active) { this.active = active; }
    public void setDescription(String description) { this.description = description; }

    public void deposit(double amount) {
        if (amount > 0) this.balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public String getFormattedBalance() {
        return String.format("$%,.2f", balance);
    }

    public String getTypeDisplay() {
        return type.name().replace("_", " ");
    }

    @Override
    public String toString() {
        return accountNumber + " - " + getTypeDisplay() + " (" + getFormattedBalance() + ")";
    }
}
