package banking;

import banking.data.DataManager;
import banking.models.*;
import banking.services.BankingService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for BankingService deposit and withdraw operations.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankingServiceOperationsTest {

    private BankingService bankingService;
    private User john;
    private Account savingsAccount;

    @BeforeAll
    static void resetData() {
        DataManager.resetInstance();
        DataManager.getInstance();
    }

    @BeforeEach
    void setUp() {
        DataManager dm = DataManager.getInstance();
        bankingService = new BankingService();
        john = dm.getAllUsers().stream()
                .filter(u -> u.getUsername().equals("john")).findFirst().orElseThrow();
        savingsAccount = dm.getAccountsByUser(john.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();
    }

    // ====== deposit ======

    @Test
    @Order(1)
    void deposit_validAmount_increasesBalance() {
        double before = savingsAccount.getBalance();
        BankingService.TransactionResult result = bankingService.deposit(
                savingsAccount.getId(), john.getId(), 500, "test deposit", "ONLINE");
        assertTrue(result.isSuccess());
        assertEquals(before + 500, savingsAccount.getBalance(), 0.01);
    }

    @Test
    @Order(2)
    void deposit_zeroAmount_fails() {
        BankingService.TransactionResult result = bankingService.deposit(
                savingsAccount.getId(), john.getId(), 0, "test", "ONLINE");
        assertFalse(result.isSuccess());
    }

    @Test
    @Order(3)
    void deposit_overMillion_fails() {
        BankingService.TransactionResult result = bankingService.deposit(
                savingsAccount.getId(), john.getId(), 1_500_000, "test", "ONLINE");
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("1,000,000"));
    }

    // ====== withdraw ======

    @Test
    @Order(4)
    void withdraw_validAmount_decreasesBalance() {
        double before = savingsAccount.getBalance();
        BankingService.TransactionResult result = bankingService.withdraw(
                savingsAccount.getId(), john.getId(), 100, "test withdraw", "ONLINE");
        assertTrue(result.isSuccess());
        assertEquals(before - 100, savingsAccount.getBalance(), 0.01);
    }

    @Test
    @Order(5)
    void withdraw_zeroAmount_fails() {
        BankingService.TransactionResult result = bankingService.withdraw(
                savingsAccount.getId(), john.getId(), 0, "test", "ONLINE");
        assertFalse(result.isSuccess());
    }

    @Test
    @Order(6)
    void withdraw_belowMinBalance_fails() {
        // Savings min balance is $500
        BankingService.TransactionResult result = bankingService.withdraw(
                savingsAccount.getId(), john.getId(),
                savingsAccount.getBalance(), "test", "ONLINE");
        assertFalse(result.isSuccess());
    }

    // ====== createAccount uniqueness ======

    @Test
    @Order(7)
    void createAccount_generatesUniqueNumbers() {
        java.util.Set<String> numbers = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Account acc = bankingService.createAccount(john.getId(), Account.AccountType.SAVINGS, 100);
            assertNotNull(acc);
            assertTrue(numbers.add(acc.getAccountNumber()), "Account number collision detected!");
        }
    }
}
