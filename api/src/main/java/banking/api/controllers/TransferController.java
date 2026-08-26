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
            return ResponseEntity.ok(res.getTransaction());
        }
        return ResponseEntity.badRequest().body(res != null ? res.getErrorMessage() : "Transfer failed");
    }

    public record TransferRequest(String fromAccountId, String toAccountNumber, double amount, String description) {}
}
