package banking.api.controllers;

import banking.services.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    @Autowired
    private BankingService bankingService;

    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody TransferRequest req, @RequestAttribute("userId") String authUserId) {
        BankingService.TransactionResult res = bankingService.transfer(
                req.fromAccountId(), req.toAccountNumber(), authUserId, req.amount(),
                req.description(), false, null, null);
        
        if (res != null && res.isSuccess()) {
            banking.models.Transaction t = res.getTransaction();
            return ResponseEntity.ok(new TransactionController.TransactionDto(
                t.getId(),
                t.getReceiptNumber(),
                t.getAccountId(),
                t.getUserId(),
                t.getType().name(),
                t.getAmount(),
                t.getBalanceAfter(),
                t.getDescription(),
                t.getRelatedAccountId(),
                t.getTimestamp(),
                t.getChannel()
            ));
        }
        return ResponseEntity.badRequest().body(res != null ? res.getErrorMessage() : "Transfer failed");
    }

    public record TransferRequest(String fromAccountId, String toAccountNumber, double amount, String description) {}
}
