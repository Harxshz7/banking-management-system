package banking;

import banking.data.DataManager;
import banking.models.User;
import banking.services.AuthService;
import banking.util.PasswordUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServicePinMigrationTest {

    private AuthService authService;
    private DataManager dm;

    @BeforeAll
    static void resetData() {
        banking.data.DatabaseManager.setDbUrl("jdbc:sqlite::memory:");
        DataManager.resetInstance();
        DataManager.getInstance();
    }

    @BeforeEach
    void setUp() {
        dm = DataManager.getInstance();
        authService = new AuthService();
    }

    @Test
    void testPinMigrationFlow() throws Exception {
        User legacyUser = new User("legacy", "oldpass", "Legacy User", "legacy@email.com", "123", "CUSTOMER");
        
        // Use reflection to seed a plaintext PIN since setTransactionPin hashes it automatically
        java.lang.reflect.Field pinField = User.class.getDeclaredField("transactionPin");
        pinField.setAccessible(true);
        pinField.set(legacyUser, "1234");
        
        assertTrue(legacyUser.hasPinSet());
        assertTrue(legacyUser.needsPinMigration(), "User with plaintext PIN should need migration");
        
        // Simulate the migration flow from LoginFrame
        legacyUser.hashAndSetPin("9999");
        
        assertFalse(legacyUser.needsPinMigration(), "User should no longer need migration after hashAndSetPin");
        assertTrue(legacyUser.verifyPin("9999"));
    }
}
