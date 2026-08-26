package banking;

import banking.data.DataManager;
import banking.models.Account;
import banking.models.Loan;
import banking.models.User;
import banking.services.BankingService;
import banking.services.LoanService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {
    private LoanService loanService;
    private User john;
    private Account johnChecking;
    private Loan activeLoan;

    @BeforeAll
    static void resetData() {
        DataManager.resetInstance();
        DataManager.getInstance();
    }

    @BeforeEach
    void setUp() {
        DataManager dm = DataManager.getInstance();
        loanService = new LoanService();
        john = dm.getAllUsers().stream().filter(u -> u.getUsername().equals("john")).findFirst().orElseThrow();
        johnChecking = dm.getAccountsByUser(john.getId()).stream().filter(a -> a.getType() == Account.AccountType.CHECKING).findFirst().orElseThrow();
        
        // Ensure John has an active loan
        activeLoan = loanService.applyForLoan(john.getId(), johnChecking.getId(), Loan.LoanType.PERSONAL, 1000, 12, "Test Loan");
        loanService.approveLoan(activeLoan.getId(), "Approved for testing");
    }

    @Test
    void makeRepayment_overpayment_fails() {
        double outstanding = activeLoan.getOutstandingAmount();
        double overpayment = outstanding + 500;
        
        // Ensure account has enough balance for the test
        johnChecking.deposit(overpayment + 1000);
        DataManager.getInstance().updateAccount(johnChecking);

        BankingService.TransactionResult result = loanService.makeRepayment(activeLoan.getId(), johnChecking.getId(), john.getId(), overpayment);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Amount exceeds outstanding balance"));
    }
}
