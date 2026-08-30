package banking.api.controllers;

import banking.models.Transaction;
import banking.services.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private BankingService bankingService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getTransactions(@PathVariable String userId, @RequestAttribute("userId") String authUserId) {
        if (!userId.equals(authUserId)) return ResponseEntity.status(403).body("Access denied");
        List<TransactionDto> transactions = bankingService.getUserTransactions(userId).stream()
                .map(t -> new TransactionDto(
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
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    public record TransactionDto(
        String id,
        String receiptNumber,
        String accountId,
        String userId,
        String type,
        double amount,
        double balanceAfter,
        String description,
        String relatedAccountId,
        LocalDateTime timestamp,
        String channel
    ) {}
}
