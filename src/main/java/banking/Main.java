package banking;

import banking.services.AuthService;
import banking.ui.LoginFrame;
import banking.ui.Theme;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Enable HiDPI scaling
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            Theme.setupLookAndFeel();
            AuthService authService = new AuthService();
            new LoginFrame(authService);
        });
    }
}
