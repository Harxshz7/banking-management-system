package banking.services;

import banking.data.DataManager;
import banking.models.User;
import banking.util.PasswordUtil;
import java.util.Optional;

public class AuthService {
    private final DataManager dm;
    private User currentUser;

    public AuthService() {
        this.dm = DataManager.getInstance();
    }

    public User login(String username, String password) {
        Optional<User> user = dm.findUserByUsername(username);
        if (user.isEmpty() || !user.get().isActive()) return null;

        User u = user.get();
        if (!PasswordUtil.check(password, u.getPassword())) return null;

        // One-time migration: re-hash plaintext password/PIN on first successful login
        if (u.needsPasswordMigration()) {
            u.hashAndSetPassword(password);
        }
        if (u.needsPinMigration()) {
            // We can't recover the plaintext PIN from the legacy plaintext,
            // so we only migrate the password here. PIN migration happens
            // when the user re-sets their PIN through the UI.
        }
        dm.saveAll();

        currentUser = u;
        return currentUser;
    }

    public boolean register(String username, String password, String fullName,
                            String email, String phone) {
        if (username.isBlank() || password.isBlank() || fullName.isBlank()) return false;
        if (dm.findUserByUsername(username).isPresent()) return false;
        User newUser = new User(username, PasswordUtil.hash(password), fullName, email, phone, "CUSTOMER");
        return dm.addUser(newUser);
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
