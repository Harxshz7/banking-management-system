package banking.ui;

import banking.models.User;
import banking.services.AuthService;
import banking.ui.components.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginFrame extends JFrame {
    private final AuthService authService;
    private JPanel loginPanel;
    private JPanel registerPanel;
    private JPanel currentPanel;

    // Login fields
    private StyledTextField loginUsername;
    private StyledPasswordField loginPassword;
    private JLabel loginError;

    // Register fields
    private StyledTextField regUsername;
    private StyledPasswordField regPassword;
    private StyledPasswordField regConfirmPassword;
    private StyledTextField regFullName;
    private StyledTextField regEmail;
    private StyledTextField regPhone;
    private JLabel regError;

    public LoginFrame(AuthService authService) {
        this.authService = authService;
        Theme.setupLookAndFeel();
        setTitle("BankPro — Secure Banking System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(new BackgroundPanel());
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new CardLayout());
        container.setOpaque(false);

        loginPanel = buildLoginPanel();
        registerPanel = buildRegisterPanel();
        container.add(loginPanel, "LOGIN");
        container.add(registerPanel, "REGISTER");

        add(container, BorderLayout.CENTER);
        currentPanel = loginPanel;
        setVisible(true);
    }

    private JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        CardPanel card = new CardPanel();
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Logo & Title
        JLabel icon = new JLabel("🏦", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("BankPro", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Secure Banking Management System", SwingConstants.CENTER);
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        loginUsername = new StyledTextField("Username");
        loginPassword = new StyledPasswordField("Password");
        loginError = new JLabel(" ");
        loginError.setForeground(Theme.DANGER);
        loginError.setFont(Theme.FONT_SMALL);
        loginError.setAlignmentX(Component.CENTER_ALIGNMENT);

        GradientButton loginBtn = new GradientButton("Sign In");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.addActionListener(e -> performLogin());

        JButton switchBtn = new JButton("Don't have an account? Register →");
        switchBtn.setOpaque(false); switchBtn.setContentAreaFilled(false);
        switchBtn.setBorderPainted(false); switchBtn.setFocusPainted(false);
        switchBtn.setForeground(Theme.ACCENT_BLUE); switchBtn.setFont(Theme.FONT_SMALL);
        switchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        switchBtn.addActionListener(e -> switchToRegister());

        JLabel hint = new JLabel("Demo: admin/admin123  or  john/john123", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(icon); card.add(Box.createVerticalStrut(8));
        card.add(title); card.add(Box.createVerticalStrut(4));
        card.add(subtitle); card.add(Box.createVerticalStrut(28));
        card.add(makeLabel("Username")); card.add(Box.createVerticalStrut(6));
        card.add(loginUsername); card.add(Box.createVerticalStrut(14));
        card.add(makeLabel("Password")); card.add(Box.createVerticalStrut(6));
        card.add(loginPassword); card.add(Box.createVerticalStrut(10));
        card.add(loginError); card.add(Box.createVerticalStrut(10));
        card.add(loginBtn); card.add(Box.createVerticalStrut(18));
        card.add(switchBtn); card.add(Box.createVerticalStrut(14));
        card.add(hint);

        Dimension cardSize = new Dimension(400, 520);
        card.setPreferredSize(cardSize);
        card.setMaximumSize(cardSize);

        wrapper.add(card);
        return wrapper;
    }

    private JPanel buildRegisterPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        CardPanel card = new CardPanel();
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        regFullName = new StyledTextField("Full Name");
        regUsername = new StyledTextField("Username");
        regEmail = new StyledTextField("Email Address");
        regPhone = new StyledTextField("Phone Number");
        regPassword = new StyledPasswordField("Password");
        regConfirmPassword = new StyledPasswordField("Confirm Password");

        regError = new JLabel(" ");
        regError.setForeground(Theme.DANGER);
        regError.setFont(Theme.FONT_SMALL);
        regError.setAlignmentX(Component.CENTER_ALIGNMENT);

        GradientButton regBtn = new GradientButton("Create Account", Theme.ACCENT_TEAL, Theme.ACCENT_BLUE);
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        regBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        regBtn.addActionListener(e -> performRegister());

        JButton backBtn = new JButton("← Back to Sign In");
        backBtn.setOpaque(false); backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false); backBtn.setFocusPainted(false);
        backBtn.setForeground(Theme.ACCENT_BLUE); backBtn.setFont(Theme.FONT_SMALL);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> switchToLogin());

        card.add(title); card.add(Box.createVerticalStrut(20));
        card.add(makeLabel("Full Name")); card.add(Box.createVerticalStrut(5));
        card.add(regFullName); card.add(Box.createVerticalStrut(10));
        card.add(makeLabel("Username")); card.add(Box.createVerticalStrut(5));
        card.add(regUsername); card.add(Box.createVerticalStrut(10));
        card.add(makeLabel("Email")); card.add(Box.createVerticalStrut(5));
        card.add(regEmail); card.add(Box.createVerticalStrut(10));
        card.add(makeLabel("Phone")); card.add(Box.createVerticalStrut(5));
        card.add(regPhone); card.add(Box.createVerticalStrut(10));
        card.add(makeLabel("Password")); card.add(Box.createVerticalStrut(5));
        card.add(regPassword); card.add(Box.createVerticalStrut(10));
        card.add(makeLabel("Confirm Password")); card.add(Box.createVerticalStrut(5));
        card.add(regConfirmPassword); card.add(Box.createVerticalStrut(8));
        card.add(regError); card.add(Box.createVerticalStrut(8));
        card.add(regBtn); card.add(Box.createVerticalStrut(12));
        card.add(backBtn);

        Dimension cardSize = new Dimension(400, 600);
        card.setPreferredSize(cardSize);
        card.setMaximumSize(cardSize);

        wrapper.add(card);
        return wrapper;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    private void performLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            loginError.setText("Please enter username and password.");
            return;
        }
        User user = authService.login(username, password);
        if (user != null) {
            dispose();
            if (user.isAdmin()) {
                new AdminDashboard(authService);
            } else {
                new CustomerDashboard(authService);
            }
        } else {
            loginError.setText("Invalid username or password.");
            loginPassword.setText("");
        }
    }

    private void performRegister() {
        String fullName = regFullName.getText().trim();
        String username = regUsername.getText().trim();
        String email = regEmail.getText().trim();
        String phone = regPhone.getText().trim();
        String password = new String(regPassword.getPassword());
        String confirm = new String(regConfirmPassword.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            regError.setText("Full name, username and password are required.");
            return;
        }
        if (!password.equals(confirm)) {
            regError.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            regError.setText("Password must be at least 6 characters.");
            return;
        }
        boolean success = authService.register(username, password, fullName, email, phone);
        if (success) {
            JOptionPane.showMessageDialog(this, "Account created! Please login.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            switchToLogin();
        } else {
            regError.setText("Username already exists.");
        }
    }

    private void switchToRegister() {
        CardLayout cl = (CardLayout) ((JPanel) getContentPane().getComponent(0)).getLayout();
        cl.show((JPanel) getContentPane().getComponent(0), "REGISTER");
        setSize(500, 720);
        setLocationRelativeTo(null);
    }

    private void switchToLogin() {
        CardLayout cl = (CardLayout) ((JPanel) getContentPane().getComponent(0)).getLayout();
        cl.show((JPanel) getContentPane().getComponent(0), "LOGIN");
        setSize(500, 650);
        setLocationRelativeTo(null);
        loginError.setText(" ");
        regError.setText(" ");
    }

    // Animated gradient background
    private class BackgroundPanel extends JPanel {
        private float animOffset = 0;
        private Timer timer;

        BackgroundPanel() {
            timer = new Timer(50, e -> { animOffset += 0.005f; repaint(); });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth(), h = getHeight();
            float shift = (float)(Math.sin(animOffset) * 0.2 + 0.4);
            GradientPaint gp = new GradientPaint(0, 0, Theme.BG_DARK, w * shift, h, new Color(20, 10, 45));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // Decorative circles
            g2.setColor(Theme.alpha(Theme.ACCENT_BLUE, 15));
            g2.fillOval(-80, -80, 300, 300);
            g2.setColor(Theme.alpha(Theme.ACCENT_PURPLE, 12));
            g2.fillOval(w - 150, h - 200, 350, 350);
        }
    }
}
