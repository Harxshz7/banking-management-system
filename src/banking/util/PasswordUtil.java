package banking.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing and verifying passwords/PINs using BCrypt.
 */
public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() { }

    /**
     * Hash a plaintext password using BCrypt.
     */
    public static String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verify a plaintext password against a BCrypt hash.
     */
    public static boolean check(String plaintext, String hash) {
        if (plaintext == null || hash == null) return false;
        try {
            return BCrypt.checkpw(plaintext, hash);
        } catch (IllegalArgumentException e) {
            // Not a valid bcrypt hash (e.g. old plaintext password)
            return false;
        }
    }

    /**
     * Returns true if the stored value looks like a BCrypt hash
     * (starts with "$2a$" or "$2b$").
     */
    public static boolean isBcryptHash(String stored) {
        return stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$"));
    }
}
