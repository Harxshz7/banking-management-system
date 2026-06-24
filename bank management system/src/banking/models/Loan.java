package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum LoanStatus { PENDING, APPROVED, REJECTED, ACTIVE, CLOSED }
    public enum LoanType { PERSONAL, HOME, AUTO, EDUCATION, BUSINESS }

    private String id;
    private String userId;
    private String creditAccountId; // Account to receive loan amount
    private LoanType type;
    private double principalAmount;
    private double interestRate;     // Annual %
    private int tenureMonths;
    private double emiAmount;
    private double totalPayable;
    private double amountPaid;
    private LoanStatus status;
    private String purpose;
    private LocalDateTime appliedAt;
    private LocalDateTime processedAt;
    private String adminNote;
    private int monthsPaid;

    public Loan(String userId, String creditAccountId, LoanType type,
                double amount, int tenureMonths, String purpose) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.creditAccountId = creditAccountId;
        this.type = type;
        this.principalAmount = amount;
        this.tenureMonths = tenureMonths;
        this.purpose = purpose;
        this.status = LoanStatus.PENDING;
        this.amountPaid = 0;
        this.monthsPaid = 0;
        this.appliedAt = LocalDateTime.now();

        // Interest rate by type
        this.interestRate = switch (type) {
            case PERSONAL   -> 12.0;
            case HOME       -> 8.5;
            case AUTO       -> 10.0;
            case EDUCATION  -> 7.0;
            case BUSINESS   -> 13.5;
        };

        calculateEmi();
    }

    private void calculateEmi() {
        // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        double r = (interestRate / 100.0) / 12.0;
        if (r == 0) {
            this.emiAmount = principalAmount / tenureMonths;
        } else {
            double factor = Math.pow(1 + r, tenureMonths);
            this.emiAmount = principalAmount * r * factor / (factor - 1);
        }
        this.totalPayable = emiAmount * tenureMonths;
    }

    // ===== Loan Operations =====
    public boolean makeRepayment(double amount) {
        if (status != LoanStatus.ACTIVE || amount <= 0) return false;
        double remaining = totalPayable - amountPaid;
        double actual = Math.min(amount, remaining);
        amountPaid += actual;
        monthsPaid++;
        if (amountPaid >= totalPayable) {
            status = LoanStatus.CLOSED;
        }
        return true;
    }

    public void approve(String note) {
        this.status = LoanStatus.ACTIVE;
        this.adminNote = note;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(String note) {
        this.status = LoanStatus.REJECTED;
        this.adminNote = note;
        this.processedAt = LocalDateTime.now();
    }

    // ===== Getters =====
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getCreditAccountId() { return creditAccountId; }
    public LoanType getType() { return type; }
    public double getPrincipalAmount() { return principalAmount; }
    public double getInterestRate() { return interestRate; }
    public int getTenureMonths() { return tenureMonths; }
    public double getEmiAmount() { return emiAmount; }
    public double getTotalPayable() { return totalPayable; }
    public double getAmountPaid() { return amountPaid; }
    public double getOutstandingAmount() { return Math.max(0, totalPayable - amountPaid); }
    public LoanStatus getStatus() { return status; }
    public String getPurpose() { return purpose; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public String getAdminNote() { return adminNote; }
    public int getMonthsPaid() { return monthsPaid; }
    public int getMonthsRemaining() { return Math.max(0, tenureMonths - monthsPaid); }

    public String getFormattedEmi() { return String.format("$%,.2f", emiAmount); }
    public String getFormattedPrincipal() { return String.format("$%,.2f", principalAmount); }
    public String getFormattedOutstanding() { return String.format("$%,.2f", getOutstandingAmount()); }
    public String getTypeDisplay() { return type.name().charAt(0) + type.name().substring(1).toLowerCase() + " Loan"; }

    public double getProgressPercent() {
        return totalPayable > 0 ? (amountPaid / totalPayable) * 100 : 0;
    }
}
