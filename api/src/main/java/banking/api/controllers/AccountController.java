package banking.api.controllers;

import banking.models.Account;
import banking.services.BankingService;
import banking.services.StatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private BankingService bankingService;

    @Autowired
    private StatementService statementService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getAccounts(@PathVariable String userId, @RequestAttribute("userId") String authUserId) {
        if (!userId.equals(authUserId)) return ResponseEntity.status(403).body("Access denied");
        List<AccountDto> accounts = bankingService.getUserAccounts(userId).stream()
                .map(a -> new AccountDto(a.getId(), a.getAccountNumber(), a.getType().name(), a.getBalance(), a.isActive()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<?> deposit(@PathVariable String accountId, @RequestBody DepositRequest req, @RequestAttribute("userId") String authUserId) {
        BankingService.TransactionResult res = bankingService.deposit(accountId, authUserId, req.amount(), req.description(), "API");
        if (res != null && res.isSuccess()) {
            return ResponseEntity.ok(res.getTransaction());
        }
        return ResponseEntity.badRequest().body(res != null ? res.getErrorMessage() : "Deposit failed");
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable String accountId, @RequestBody WithdrawRequest req, @RequestAttribute("userId") String authUserId) {
        BankingService.TransactionResult res = bankingService.withdraw(accountId, authUserId, req.amount(), req.description(), "API");
        if (res != null && res.isSuccess()) {
            return ResponseEntity.ok(res.getTransaction());
        }
        return ResponseEntity.badRequest().body(res != null ? res.getErrorMessage() : "Withdraw failed");
    }

    @GetMapping("/{accountId}/statement")
    public ResponseEntity<byte[]> getStatement(
            @PathVariable String accountId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestAttribute("userId") String authUserId) {
        
        LocalDate fromDate = from != null && !from.isEmpty() ? LocalDate.parse(from) : null;
        LocalDate toDate = to != null && !to.isEmpty() ? LocalDate.parse(to) : null;

        try {
            byte[] pdf = statementService.generateStatementPdf(accountId, authUserId, fromDate, toDate);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "statement.pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public record AccountDto(String id, String accountNumber, String type, double balance, boolean active) {}
    public record DepositRequest(double amount, String description) {}
    public record WithdrawRequest(double amount, String description) {}
}
