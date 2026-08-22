package banking;

import banking.models.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Account.tryWithdraw() business rules.
 * Each test creates its own Account instances to avoid shared-state issues.
 */
class AccountTest {

    /** Helper: create a SAVINGS account with given balance. */
    private Account makeSavings(double balance) {
        return new Account("user1", Account.AccountType.SAVINGS, balance);
    }

    /** Helper: create a CHECKING account with given balance. */
    private Account makeChecking(double balance) {
        return new Account("user1", Account.AccountType.CHECKING, balance);
    }

    /** Helper: create a FIXED_DEPOSIT account with given balance. */
    private Account makeFD(double balance) {
        return new Account("user1", Account.AccountType.FIXED_DEPOSIT, balance);
    }

    // ====== tryWithdraw: zero/negative amount ======

    @Test
    void tryWithdraw_zeroAmount_returnsError() {
        Account acc = makeSavings(5000);
        String error = acc.tryWithdraw(0);
        assertNotNull(error);
        assertTrue(error.contains("greater than zero"));
    }

    @Test
    void tryWithdraw_negativeAmount_returnsError() {
        Account acc = makeSavings(5000);
        String error = acc.tryWithdraw(-100);
        assertNotNull(error);
        assertTrue(error.contains("greater than zero"));
    }

    // ====== tryWithdraw: minimum balance ======

    @Test
    void tryWithdraw_belowMinBalance_returnsError() {
        // SAVINGS: min balance $500, balance $5000
        Account acc = makeSavings(5000);
        // Withdrawing $4600 would leave $400 < $500
        String error = acc.tryWithdraw(4600);
        assertNotNull(error);
        assertTrue(error.contains("Minimum balance"));
    }

    @Test
    void tryWithdraw_atMinBalance_succeeds() {
        // SAVINGS: min balance $500, balance $5000
        // Withdraw $4500 leaves exactly $500
        Account acc = makeSavings(5000);
        String error = acc.tryWithdraw(4500);
        assertNull(error);
        assertEquals(500.0, acc.getBalance(), 0.01);
    }

    @Test
    void tryWithdraw_aboveMinBalance_succeeds() {
        // SAVINGS: min balance $500, balance $5000
        // Withdraw $1000 leaves $4000 > $500
        Account acc = makeSavings(5000);
        String error = acc.tryWithdraw(1000);
        assertNull(error);
        assertEquals(4000.0, acc.getBalance(), 0.01);
    }

    // ====== tryWithdraw: daily withdrawal limit ======

    @Test
    void tryWithdraw_exceedsDailyLimit_returnsError() {
        // SAVINGS: daily limit $5000, balance $10000, min $500
        Account acc = makeSavings(10000);
        String error = acc.tryWithdraw(6000);
        assertNotNull(error);
        assertTrue(error.contains("Daily withdrawal limit"));
    }

    @Test
    void tryWithdraw_withinDailyLimit_succeeds() {
        // CHECKING: daily limit $10000, balance $8000, min $0
        Account acc = makeChecking(8000);
        String error = acc.tryWithdraw(8000);
        assertNull(error);
        assertEquals(0.0, acc.getBalance(), 0.01);
    }

    // ====== tryWithdraw: frozen account ======

    @Test
    void tryWithdraw_frozenAccount_returnsError() {
        Account acc = makeSavings(5000);
        acc.setFrozen(true);
        String error = acc.tryWithdraw(100);
        assertNotNull(error);
        assertTrue(error.contains("frozen"));
    }

    // ====== tryWithdraw: FD block ======

    @Test
    void tryWithdraw_fdAccount_returnsError() {
        Account acc = makeFD(25000);
        String error = acc.tryWithdraw(100);
        assertNotNull(error);
        assertTrue(error.contains("Fixed Deposit"));
    }

    // ====== tryWithdraw: insufficient balance ======

    @Test
    void tryWithdraw_insufficientBalance_returnsError() {
        // CHECKING: balance $3000, min $0
        // Note: min balance check fires before insufficient balance check,
        // so we verify withdrawal fails (either message is acceptable)
        Account acc = makeChecking(3000);
        String error = acc.tryWithdraw(5000);
        assertNotNull(error);
        // Balance should remain unchanged
        assertEquals(3000.0, acc.getBalance(), 0.01);
    }

    // ====== tryWithdraw: success path ======

    @Test
    void tryWithdraw_validAmount_deductsBalance() {
        Account acc = makeSavings(5000);
        String error = acc.tryWithdraw(1500);
        assertNull(error);
        assertEquals(3500.0, acc.getBalance(), 0.01);
    }

    @Test
    void tryWithdraw_exactlyBalanceOnChecking_succeeds() {
        // CHECKING: no min balance, so withdrawing full balance should work
        Account acc = makeChecking(3200);
        String error = acc.tryWithdraw(3200);
        assertNull(error);
        assertEquals(0.0, acc.getBalance(), 0.01);
    }

    // ====== FD maturity ======

    @Test
    void tryWithdraw_fdMaturityAmount_isNotWithdrawable() {
        Account acc = makeFD(25000);
        double maturityAmount = acc.getFixedDepositMaturityAmount();
        assertTrue(maturityAmount > acc.getBalance(),
                "Maturity amount should be greater than current balance");
        String error = acc.tryWithdraw(acc.getBalance());
        assertNotNull(error);
    }
}
