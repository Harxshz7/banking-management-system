package banking.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class RecurringDeposit implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String accountId;
    private double monthlyAmount;
    private int months;
    private double interestRate;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private double accumulatedAmount;
    private boolean active;
    private LocalDateTime createdAt;

    public RecurringDeposit(String userId, String accountId,
            double monthlyAmount, int months,
            double interestRate, LocalDate startDate) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.accountId = accountId;
        this.monthlyAmount = monthlyAmount;
        this.months = months;
        this.interestRate = interestRate;
        this.startDate = startDate;
        this.maturityDate = startDate.plusMonths(months);
        this.accumulatedAmount = 0;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getMonthlyAmount() {
        return monthlyAmount;
    }

    public int getMonths() {
        return months;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public double getAccumulatedAmount() {
        return accumulatedAmount;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void addMonthlyDeposit(double amount) {
        this.accumulatedAmount += amount;
    }

    public double getMaturityAmount() {
        double total = monthlyAmount * months;
        double interest = total * (interestRate / 100.0) * (months / 12.0);
        return total + interest;
    }

    public void close() {
        this.active = false;
    }

    public String getStatus() {
        return active ? "Active" : "Closed";
    }
}
