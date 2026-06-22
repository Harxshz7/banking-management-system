package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 2L;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT,
        ACCOUNT_OPENED, INTEREST_CREDIT, LOAN_DISBURSEMENT, LOAN_REPAYMENT, BILL_PAYMENT
    }

    private String id;
    private String receiptNumber;
    private String accountId;
    private String userId;
    private TransactionType type;
    private double amount;
    private double balanceAfter;
    private String description;
    private String relatedAccountId;
    private LocalDateTime timestamp;
    private String channel; // ATM, ONLINE, BRANCH, NEFT, IMPS, UPI

    public Transaction(String accountId, String userId, TransactionType type,
                       double amount, double balanceAfter, String description,
                       String relatedAccountId) {
        this.id = UUID.randomUUID().toString();
        this.receiptNumber = "TXN" + System.currentTimeMillis();
        this.accountId = accountId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.relatedAccountId = relatedAccountId;
        this.timestamp = LocalDateTime.now();
        this.channel = "ONLINE";
    }

    // Getters
    public String getId() { return id; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public String getRelatedAccountId() { return relatedAccountId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getFormattedAmount() {
        String prefix = isCredit() ? "+" : "-";
        return prefix + String.format("$%,.2f", amount);
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm:ss"));
    }

    public boolean isCredit() {
        return type == TransactionType.DEPOSIT
            || type == TransactionType.TRANSFER_IN
            || type == TransactionType.ACCOUNT_OPENED
            || type == TransactionType.INTEREST_CREDIT
            || type == TransactionType.LOAN_DISBURSEMENT;
    }

    public String getTypeIcon() {
        return switch (type) {
            case DEPOSIT           -> "💰";
            case WITHDRAWAL        -> "🏧";
            case TRANSFER_IN       -> "📥";
            case TRANSFER_OUT      -> "📤";
            case ACCOUNT_OPENED    -> "🎉";
            case INTEREST_CREDIT   -> "📈";
            case LOAN_DISBURSEMENT -> "🏦";
            case LOAN_REPAYMENT    -> "💳";
            case BILL_PAYMENT      -> "🧾";
        };
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s", getFormattedTimestamp(), type.name(), getFormattedAmount());
    }
}
