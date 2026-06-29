package banking.services;

import banking.data.DataManager;
import banking.models.User;
import java.util.Optional;

public class AuthService {
    private final DataManager dm;
    private User currentUser;

    public AuthService() {
        this.dm = DataManager.getInstance();
    }

    public User login(String username, String password) {
        Optional<User> user = dm.findUserByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password) && user.get().isActive()) {
            currentUser = user.get();
            return currentUser;
        }
        return null;
    }

    public boolean register(String username, String password, String fullName,
                            String email, String phone) {
        if (username.isBlank() || password.isBlank() || fullName.isBlank()) return false;
        if (dm.findUserByUsername(username).isPresent()) return false;
        User newUser = new User(username, password, fullName, email, phone, "CUSTOMER");
        return dm.addUser(newUser);
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
