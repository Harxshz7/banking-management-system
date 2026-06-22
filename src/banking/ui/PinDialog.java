package banking.ui;

import banking.models.User;
import banking.ui.components.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modal dialog that prompts for the 4-digit transaction PIN.
 * Returns true if PIN was verified, false if cancelled/wrong.
 */
public class PinDialog extends JDialog {
    private boolean verified = false;
    private final User user;
    private final JPasswordField[] digits = new JPasswordField[4];
    private JLabel errorLabel;

    public PinDialog(JFrame parent, User user) {
        super(parent, "Confirm Transaction PIN", true);
        this.user = user;

        setUndecorated(true);
        setSize(340, 340);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setOpaque(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 20, 20));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Icon + title
        JLabel icon = new JLabel("🔐", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JLabel title = new JLabel("Transaction PIN", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Enter your 4-digit security PIN", SwingConstants.CENTER);
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        // PIN boxes
        JPanel pinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pinPanel.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            digits[i] = new JPasswordField(1) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.BG_INPUT);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(isFocusOwner() ? Theme.ACCENT_BLUE : Theme.BORDER);
                    g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
                    g2.dispose();
                    super.paintComponent(g);
                }
                @Override public void paintBorder(Graphics g) {}
            };
            digits[i].setPreferredSize(new Dimension(56, 56));
            digits[i].setHorizontalAlignment(JTextField.CENTER);
            digits[i].setFont(new Font("Segoe UI", Font.BOLD, 22));
            digits[i].setForeground(Theme.TEXT_PRIMARY);
            digits[i].setCaretColor(Theme.ACCENT_BLUE);
            digits[i].setBackground(Theme.BG_INPUT);
            digits[i].setEchoChar('●');
            digits[i].setOpaque(false);

            digits[i].addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c)) { e.consume(); return; }
                    if (digits[idx].getPassword().length >= 1) e.consume();
                    SwingUtilities.invokeLater(() -> {
                        if (digits[idx].getPassword().length == 1 && idx < 3) {
                            digits[idx + 1].requestFocusInWindow();
                        }
                        if (idx == 3) verifyPin();
                    });
                }
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && digits[idx].getPassword().length == 0 && idx > 0) {
                        digits[idx - 1].requestFocusInWindow();
                        digits[idx - 1].setText("");
                    }
                }
            });
            pinPanel.add(digits[i]);
        }

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.DANGER);

        GradientButton confirmBtn = new GradientButton("Confirm");
        confirmBtn.addActionListener(e -> verifyPin());
        confirmBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setOpaque(false); cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false); cancelBtn.setFocusPainted(false);
        cancelBtn.setForeground(Theme.TEXT_MUTED); cancelBtn.setFont(Theme.FONT_SMALL);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        pinPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        content.add(icon); content.add(Box.createVerticalStrut(8));
        content.add(title); content.add(Box.createVerticalStrut(4));
        content.add(sub); content.add(Box.createVerticalStrut(22));
        content.add(pinPanel); content.add(Box.createVerticalStrut(10));
        content.add(errorLabel); content.add(Box.createVerticalStrut(14));
        content.add(confirmBtn); content.add(Box.createVerticalStrut(8));
        content.add(cancelBtn);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        SwingUtilities.invokeLater(() -> digits[0].requestFocusInWindow());
    }

    private void verifyPin() {
        StringBuilder pin = new StringBuilder();
        for (JPasswordField d : digits) pin.append(new String(d.getPassword()));
        if (pin.length() < 4) {
            errorLabel.setText("Enter all 4 digits.");
            return;
        }
        if (user.verifyPin(pin.toString())) {
            verified = true;
            dispose();
        } else {
            errorLabel.setText("Incorrect PIN. Try again.");
            for (JPasswordField d : digits) d.setText("");
            digits[0].requestFocusInWindow();
        }
    }

    /** Show the dialog and return true if verified */
    public static boolean verify(JFrame parent, User user) {
        if (!user.hasPinSet()) return true; // No PIN set, skip
        PinDialog dlg = new PinDialog(parent, user);
        dlg.setVisible(true);
        return dlg.verified;
    }

    /** Show dialog to set a new PIN */
    public static String promptNewPin(JFrame parent) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBackground(Theme.BG_CARD);
        JPasswordField pin1 = new JPasswordField();
        JPasswordField pin2 = new JPasswordField();
        panel.add(new JLabel("New 4-digit PIN:") {{ setForeground(Theme.TEXT_PRIMARY); }});
        panel.add(pin1);
        panel.add(new JLabel("Confirm PIN:") {{ setForeground(Theme.TEXT_PRIMARY); }});
        panel.add(pin2);

        int res = JOptionPane.showConfirmDialog(parent, panel, "Set Transaction PIN",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return null;

        String p1 = new String(pin1.getPassword());
        String p2 = new String(pin2.getPassword());
        if (!p1.equals(p2)) { JOptionPane.showMessageDialog(parent, "PINs do not match."); return null; }
        if (!p1.matches("\\d{4}")) { JOptionPane.showMessageDialog(parent, "PIN must be exactly 4 digits."); return null; }
        return p1;
    }
}
