package banking.services;

import banking.data.DataManager;
import banking.models.*;
import banking.services.BankingService.TransactionResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BankingServiceExtensions {
    private final DataManager dm;
    private final BankingService bankingService;

    public BankingServiceExtensions() {
        this.dm = DataManager.getInstance();
        this.bankingService = new BankingService();
    }

    public BankingService.TransactionResult scheduleTransfer(String userId, String fromAccountId,
            String toAccountNumber, double amount,
            String description, LocalDate date,
            boolean recurringMonthly) {
        if (amount <= 0)
            return BankingService.TransactionResult.fail("Amount must be greater than zero.");
        Account account = dm.findAccountById(fromAccountId).orElse(null);
        if (account == null || !account.isActive())
            return BankingService.TransactionResult.fail("Source account not found or inactive.");
        ScheduledTransfer transfer = new ScheduledTransfer(userId, fromAccountId, toAccountNumber,
                amount, description, date, recurringMonthly);
        dm.addScheduledTransfer(transfer);
        dm.addAuditLog(new AuditLog(userId, "Scheduled transfer to " + toAccountNumber + " on " + date));
        dm.addNotification(
                new Notification(userId, "Scheduled transfer created for " + date, Notification.NotificationType.INFO));
        return BankingService.TransactionResult.success(null, account);
    }

    public List<ScheduledTransfer> getScheduledTransfers(String userId) {
        return dm.getScheduledTransfersByUser(userId);
    }

    public void processScheduledTransfers() {
        LocalDate today = LocalDate.now();
        List<ScheduledTransfer> transfers = dm.getAllScheduledTransfers();
        for (ScheduledTransfer transfer : transfers) {
            if (!transfer.isExecuted() && !transfer.getScheduledDate().isAfter(today)) {
                TransactionResult result = bankingService.transfer(
                        transfer.getFromAccountId(), transfer.getToAccountNumber(),
                        transfer.getUserId(), transfer.getAmount(), transfer.getDescription(), false, null, null);
                if (result.isSuccess()) {
                    transfer.markExecuted();
                    if (transfer.isRecurringMonthly()) {
                        transfer.rescheduleNextMonth();
                    }
                    dm.updateScheduledTransfer(transfer);
                    dm.addNotification(new Notification(transfer.getUserId(),
                            "Scheduled transfer of $" + transfer.getAmount() + " completed.",
                            Notification.NotificationType.SUCCESS));
                    dm.addAuditLog(new AuditLog(transfer.getUserId(),
                            "Executed scheduled transfer to " + transfer.getToAccountNumber()));
                }
            }
        }
    }

    public RecurringDeposit createRecurringDeposit(String userId, String accountId,
            double monthlyAmount, int months, double interestRate) {
        if (monthlyAmount <= 0 || months <= 0)
            return null;
        RecurringDeposit rd = new RecurringDeposit(userId, accountId, monthlyAmount, months, interestRate,
                LocalDate.now());
        dm.addRd(rd);
        dm.addAuditLog(new AuditLog(userId, "Created recurring deposit for " + months + " months."));
        dm.addNotification(
                new Notification(userId, "Recurring deposit opened.", Notification.NotificationType.SUCCESS));
        return rd;
    }

    public boolean makeRecurringDepositPayment(String rdId, double amount) {
        RecurringDeposit rd = dm.getAllRds().stream().filter(r -> r.getId().equals(rdId)).findFirst().orElse(null);
        if (rd == null || !rd.isActive())
            return false;
        rd.addMonthlyDeposit(amount);
        dm.updateRd(rd);
        dm.addAuditLog(new AuditLog(rd.getUserId(), "Added recurring deposit payment of $" + amount));
        dm.addNotification(new Notification(rd.getUserId(), "Recurring deposit payment received.",
                Notification.NotificationType.INFO));
        return true;
    }
}
