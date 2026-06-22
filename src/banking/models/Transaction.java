package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, ACCOUNT_OPENED
    }

    private String id;
    private String accountId;
    private String userId;
    private TransactionType type;
    private double amount;
    private double balanceAfter;
    private String description;
    private String relatedAccountId;
    private LocalDateTime timestamp;

    public Transaction(String accountId, String userId, TransactionType type,
                       double amount, double balanceAfter, String description, String relatedAccountId) {
        this.id = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.relatedAccountId = relatedAccountId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public String getRelatedAccountId() { return relatedAccountId; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedAmount() {
        String prefix = (type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN ||
                         type == TransactionType.ACCOUNT_OPENED) ? "+" : "-";
        return prefix + String.format("$%,.2f", amount);
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm:ss"));
    }

    public boolean isCredit() {
        return type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN ||
               type == TransactionType.ACCOUNT_OPENED;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s", getFormattedTimestamp(), type.name(), getFormattedAmount());
    }
}
