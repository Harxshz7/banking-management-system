package banking.api.controllers;

import banking.models.Loan;
import banking.services.BankingService;
import banking.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getLoans(@PathVariable String userId, @RequestAttribute("userId") String authUserId) {
        if (!userId.equals(authUserId)) return ResponseEntity.status(403).body("Access denied");
        List<LoanDto> loans = loanService.getUserLoans(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody LoanApplyRequest req, @RequestAttribute("userId") String authUserId) {
        Loan loan = loanService.applyForLoan(authUserId, req.accountId(), req.type(), req.amount(), req.tenureMonths(), req.purpose());
        if (loan != null) {
            return ResponseEntity.ok(toDto(loan));
        }
        return ResponseEntity.badRequest().body("Loan application failed. Check amount limits or maximum active loan count.");
    }

    @PostMapping("/{loanId}/repay")
    public ResponseEntity<?> repay(@PathVariable String loanId, @RequestBody LoanRepayRequest req, @RequestAttribute("userId") String authUserId) {
        BankingService.TransactionResult res = loanService.makeRepayment(loanId, req.accountId(), authUserId, req.amount());
        if (res != null && res.isSuccess()) {
            Loan loan = loanService.getLoanById(loanId);
            return ResponseEntity.ok(loan != null ? toDto(loan) : "Repayment successful");
        }
        return ResponseEntity.badRequest().body(res != null ? res.getErrorMessage() : "Loan repayment failed");
    }

    private LoanDto toDto(Loan l) {
        return new LoanDto(
            l.getId(),
            l.getUserId(),
            l.getCreditAccountId(),
            l.getType().name(),
            l.getPrincipalAmount(),
            l.getInterestRate(),
            l.getTenureMonths(),
            l.getEmiAmount(),
            l.getTotalPayable(),
            l.getAmountPaid(),
            l.getOutstandingAmount(),
            l.getStatus().name(),
            l.getPurpose()
        );
    }

    public record LoanApplyRequest(String accountId, Loan.LoanType type, double amount, int tenureMonths, String purpose) {}
    public record LoanRepayRequest(String accountId, double amount) {}
    public record LoanDto(String id, String userId, String creditAccountId, String type, double principalAmount, double interestRate, int tenureMonths, double emiAmount, double totalPayable, double amountPaid, double outstandingAmount, String status, String purpose) {}
}
