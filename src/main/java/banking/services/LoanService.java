package banking.services;

import banking.data.DataManager;
import banking.models.*;
import java.util.List;

public class LoanService {
    private final DataManager dm;
    private final BankingService bankingService;

    public LoanService() {
        this.dm = DataManager.getInstance();
        this.bankingService = new BankingService();
    }

    public Loan applyForLoan(String userId, String creditAccountId,
                              Loan.LoanType type, double amount,
                              int tenureMonths, String purpose) {
        if (amount <= 0 || tenureMonths <= 0) return null;
        if (amount > 500_000) return null; // Max loan limit

        // Check if user has an active pending or active loan
        long activeLoans = dm.getLoansByUser(userId).stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.PENDING ||
                             l.getStatus() == Loan.LoanStatus.ACTIVE).count();
        if (activeLoans >= 3) return null; // Max 3 active loans

        Loan loan = new Loan(userId, creditAccountId, type, amount, tenureMonths, purpose);
        dm.addLoan(loan);
        return loan;
    }

    public boolean approveLoan(String loanId, String adminNote) {
        Loan loan = dm.findLoanById(loanId).orElse(null);
        if (loan == null || loan.getStatus() != Loan.LoanStatus.PENDING) return false;

        loan.approve(adminNote);

        // Credit the loan amount to the account
        Account acc = dm.findAccountById(loan.getCreditAccountId()).orElse(null);
        if (acc != null) {
            acc.deposit(loan.getPrincipalAmount());
            dm.updateAccount(acc);

            Transaction tx = new Transaction(acc.getId(), loan.getUserId(),
                    Transaction.TransactionType.LOAN_DISBURSEMENT, loan.getPrincipalAmount(),
                    acc.getBalance(), loan.getTypeDisplay() + " Disbursed", null);
            dm.addTransaction(tx);
        }

        dm.updateLoan(loan);
        return true;
    }

    public boolean rejectLoan(String loanId, String adminNote) {
        Loan loan = dm.findLoanById(loanId).orElse(null);
        if (loan == null || loan.getStatus() != Loan.LoanStatus.PENDING) return false;
        loan.reject(adminNote);
        dm.updateLoan(loan);
        return true;
    }

    public BankingService.TransactionResult makeRepayment(String loanId, String debitAccountId,
                                                           String userId, double amount) {
        Loan loan = dm.findLoanById(loanId).orElse(null);
        if (loan == null || loan.getStatus() != Loan.LoanStatus.ACTIVE)
            return BankingService.TransactionResult.fail("Loan not found or not active.");

        double outstanding = loan.getOutstandingAmount();
        double payAmount = Math.min(amount, outstanding);

        // Debit from account
        Account acc = dm.findAccountById(debitAccountId).orElse(null);
        if (acc == null) return BankingService.TransactionResult.fail("Account not found.");

        String withdrawErr = acc.tryWithdraw(payAmount);
        if (withdrawErr != null) return BankingService.TransactionResult.fail(withdrawErr);

        dm.updateAccount(acc);
        loan.makeRepayment(payAmount);
        dm.updateLoan(loan);

        Transaction tx = new Transaction(acc.getId(), userId,
                Transaction.TransactionType.LOAN_REPAYMENT, payAmount, acc.getBalance(),
                "Loan Repayment — " + loan.getTypeDisplay() + 
                " | Remaining: " + loan.getFormattedOutstanding(), null);
        dm.addTransaction(tx);

        return BankingService.TransactionResult.success(tx, acc);
    }

    public List<Loan> getUserLoans(String userId) { return dm.getLoansByUser(userId); }
    public List<Loan> getAllLoans() { return dm.getAllLoans(); }

    public Loan getLoanById(String id) { return dm.findLoanById(id).orElse(null); }
}
