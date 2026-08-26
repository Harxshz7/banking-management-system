package banking;

import banking.data.DataManager;
import banking.models.*;
import banking.services.BankingService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BankingService.transfer() business rules.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankingServiceTest {

    private BankingService bankingService;
    private User john;
    private User jane;
    private Account johnSavings;
    private Account janeSavings;
    private Account johnChecking;

    @BeforeAll
    static void resetData() {
        DataManager.resetInstance();
        DataManager.getInstance(); // re-seed
    }

    @BeforeEach
    void setUp() {
        DataManager dm = DataManager.getInstance();
        bankingService = new BankingService();

        john = dm.getAllUsers().stream()
                .filter(u -> u.getUsername().equals("john")).findFirst().orElseThrow();
        jane = dm.getAllUsers().stream()
                .filter(u -> u.getUsername().equals("jane")).findFirst().orElseThrow();

        johnSavings = dm.getAccountsByUser(john.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();
        johnChecking = dm.getAccountsByUser(john.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.CHECKING)
                .findFirst().orElseThrow();
        janeSavings = dm.getAccountsByUser(jane.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();
    }

    // ====== transfer: same account ======

    @Test
    @Order(1)
    void transfer_toSameAccount_fails() {
        BankingService.TransactionResult result = bankingService.transfer(
                johnSavings.getId(), johnSavings.getAccountNumber(),
                john.getId(), 100, "test", false, null, null);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("same account"));
    }

    // ====== transfer: frozen destination ======

    @Test
    @Order(2)
    void transfer_toFrozenAccount_fails() {
        janeSavings.setFrozen(true);
        try {
            BankingService.TransactionResult result = bankingService.transfer(
                    johnSavings.getId(), janeSavings.getAccountNumber(),
                    john.getId(), 100, "test", false, null, null);
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("frozen"));
        } finally {
            janeSavings.setFrozen(false);
        }
    }

    // ====== transfer: insufficient funds ======

    @Test
    @Order(3)
    void transfer_insufficientFunds_fails() {
        double balance = johnChecking.getBalance();
        BankingService.TransactionResult result = bankingService.transfer(
                johnChecking.getId(), janeSavings.getAccountNumber(),
                john.getId(), balance + 1000, "test", false, null, null);
        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    // ====== transfer: success path ======

    @Test
    @Order(4)
    void transfer_validAmount_movesFunds() {
        double fromBefore = johnSavings.getBalance();
        double toBefore = janeSavings.getBalance();
        double transferAmount = 200;

        BankingService.TransactionResult result = bankingService.transfer(
                johnSavings.getId(), janeSavings.getAccountNumber(),
                john.getId(), transferAmount, "test transfer", false, null, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(fromBefore - transferAmount, johnSavings.getBalance(), 0.01);
        assertEquals(toBefore + transferAmount, janeSavings.getBalance(), 0.01);
    }

    // ====== transfer: negative/zero amount ======

    @Test
    @Order(5)
    void transfer_zeroAmount_fails() {
        BankingService.TransactionResult result = bankingService.transfer(
                johnSavings.getId(), janeSavings.getAccountNumber(),
                john.getId(), 0, "test", false, null, null);
        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    @Test
    @Order(6)
    void transfer_negativeAmount_fails() {
        BankingService.TransactionResult result = bankingService.transfer(
                johnSavings.getId(), janeSavings.getAccountNumber(),
                john.getId(), -500, "test", false, null, null);
        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    // ====== transfer: non-existent destination ======

    @Test
    @Order(7)
    void transfer_nonExistentDestination_fails() {
        BankingService.TransactionResult result = bankingService.transfer(
                johnSavings.getId(), "9999999999",
                john.getId(), 100, "test", false, null, null);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("not found"));
    }

    // ====== transfer: with beneficiary ======

    @Test
    @Order(8)
    void transfer_withSaveBeneficiary_createsBeneficiary() {
        // Create a unique account number that definitely has no beneficiary yet
        Account freshTarget = new Account(jane.getId(), Account.AccountType.SAVINGS, 100);
        DataManager.getInstance().addAccount(freshTarget);

        int before = bankingService.getBeneficiaries(john.getId()).size();

        BankingService.TransactionResult result = bankingService.transfer(
                johnSavings.getId(), freshTarget.getAccountNumber(),
                john.getId(), 10, "test", true, "Test Beneficiary", "TB");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        int after = bankingService.getBeneficiaries(john.getId()).size();
        assertEquals(before + 1, after);
    }

    // ====== transfer atomicity ======

    @Test
    @Order(9)
    void transfer_atomicity_callsSaveAllExactlyOnce() {
        DataManager dm = DataManager.getInstance();
        dm.resetSaveAllCount();

        BankingService.TransactionResult result = bankingService.transfer(
                johnSavings.getId(), janeSavings.getAccountNumber(),
                john.getId(), 50, "test atomicity", true, "Jane Beneficiary", "Jane");

        assertTrue(result.isSuccess());
        assertEquals(1, dm.getSaveAllCount(), "Transfer should trigger exactly one saveAll() call");
    }
}
