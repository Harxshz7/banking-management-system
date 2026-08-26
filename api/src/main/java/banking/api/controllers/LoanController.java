package banking.api.controllers;

import banking.models.Loan;
import banking.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getLoans(@PathVariable String userId, @RequestAttribute("userId") String authUserId) {
        if (!userId.equals(authUserId)) return ResponseEntity.status(403).body("Access denied");
        List<Loan> loans = loanService.getUserLoans(userId);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody LoanApplyRequest req, @RequestAttribute("userId") String authUserId) {
        LoanService.LoanResult res = loanService.applyForLoan(authUserId, req.accountId(), req.type(), req.amount(), req.tenureMonths(), req.purpose());
        if (res != null && res.isSuccess()) {
            return ResponseEntity.ok(res.getLoan());
        }
        return ResponseEntity.badRequest().body(res != null ? res.getErrorMessage() : "Loan application failed");
    }

    @PostMapping("/{loanId}/repay")
    public ResponseEntity<?> repay(@PathVariable String loanId, @RequestBody LoanRepayRequest req, @RequestAttribute("userId") String authUserId) {
        LoanService.LoanResult res = loanService.repayLoan(loanId, req.accountId(), authUserId, req.amount());
        if (res != null && res.isSuccess()) {
            return ResponseEntity.ok(res.getLoan());
        }
        return ResponseEntity.badRequest().body(res != null ? res.getErrorMessage() : "Loan repayment failed");
    }

    public record LoanApplyRequest(String accountId, Loan.LoanType type, double amount, int tenureMonths, String purpose) {}
    public record LoanRepayRequest(String accountId, double amount) {}
}
