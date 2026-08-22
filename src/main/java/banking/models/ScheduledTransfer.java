package banking.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduledTransfer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String fromAccountId;
    private String toAccountNumber;
    private double amount;
    private String description;
    private LocalDate scheduledDate;
    private boolean recurringMonthly;
    private boolean executed;
    private LocalDateTime createdAt;

    public ScheduledTransfer(String userId, String fromAccountId, String toAccountNumber,
            double amount, String description, LocalDate scheduledDate,
            boolean recurringMonthly) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.fromAccountId = fromAccountId;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.description = description;
        this.scheduledDate = scheduledDate;
        this.recurringMonthly = recurringMonthly;
        this.executed = false;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public boolean isRecurringMonthly() {
        return recurringMonthly;
    }

    public boolean isExecuted() {
        return executed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void markExecuted() {
        this.executed = true;
    }

    public void rescheduleNextMonth() {
        this.scheduledDate = this.scheduledDate.plusMonths(1);
    }

    public String getStatus() {
        if (executed && !recurringMonthly)
            return "Completed";
        return recurringMonthly ? "Recurring" : "Pending";
    }
}
