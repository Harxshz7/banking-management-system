package banking;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Enable HiDPI scaling
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Process any due scheduled transfers on startup
        try {
            new BankingServiceExtensions().processScheduledTransfers();
        } catch (Exception e) {
            System.err.println("Error processing scheduled transfers: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            Theme.setupLookAndFeel();
            AuthService authService = new AuthService();
            new LoginFrame(authService);
        });
    }
}
