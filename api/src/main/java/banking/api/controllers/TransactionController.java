package banking.api.controllers;

import banking.models.Transaction;
import banking.services.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private BankingService bankingService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getTransactions(@PathVariable String userId, @RequestAttribute("userId") String authUserId) {
        if (!userId.equals(authUserId)) return ResponseEntity.status(403).body("Access denied");
        List<Transaction> transactions = bankingService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
}
