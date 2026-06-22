package banking.ui;

import banking.models.Transaction;
import banking.ui.components.CardPanel;
import banking.ui.components.GradientButton;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ReceiptDialog extends JDialog {

    public ReceiptDialog(JFrame parent, Transaction tx, String accountNumber, String ownerName) {
        super(parent, "Transaction Receipt", true);
        setUndecorated(true);
        setSize(400, 520);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

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
        root.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        // Header
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color s = tx.isCredit() ? Theme.SUCCESS : Theme.DANGER;
                Color e2 = tx.isCredit() ? Theme.ACCENT_TEAL : new Color(180, 40, 60);
                g2.setPaint(new GradientPaint(0, 0, s, getWidth(), 0, e2));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(Theme.alpha(Color.WHITE, 20));
                g2.fillRect(0, getHeight() / 2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(400, 140));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));
        header.setOpaque(false);

        JLabel icon = new JLabel(tx.getTypeIcon() + "  " +
                (tx.isCredit() ? "CREDITED" : "DEBITED"), SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel amountLabel = new JLabel(tx.getFormattedAmount(), SwingConstants.CENTER);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
        amountLabel.setForeground(Color.WHITE);
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel successLabel = new JLabel("✓ Transaction Successful", SwingConstants.CENTER);
        successLabel.setFont(Theme.FONT_BODY);
        successLabel.setForeground(Theme.alpha(Color.WHITE, 200));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(6));
        header.add(amountLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(successLabel);

        // Body rows
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        addRow(body, "Receipt No.",     tx.getReceiptNumber());
        addRow(body, "Date & Time",     tx.getFormattedTimestamp());
        addRow(body, "Account No.",     accountNumber);
        addRow(body, "Account Holder",  ownerName);
        addRow(body, "Transaction Type",tx.getType().name().replace("_", " "));
        addRow(body, "Description",     tx.getDescription());
        addRow(body, "Balance After",   String.format("$%,.2f", tx.getBalanceAfter()));
        addRow(body, "Channel",         tx.getChannel());

        // Dashed separator line
        JSeparator sep = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Theme.BORDER);
                float[] dash = {6f, 4f};
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10, dash, 0));
                g2.drawLine(0, 0, getWidth(), 0);
            }
        };
        sep.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Close button
        GradientButton closeBtn = new GradientButton("Close Receipt");
        closeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        closeBtn.addActionListener(e -> dispose());

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Thank you for banking with BankPro", SwingConstants.CENTER);
        tagline.setFont(Theme.FONT_SMALL);
        tagline.setForeground(Theme.TEXT_MUTED);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.add(closeBtn);
        footer.add(Box.createVerticalStrut(10));
        footer.add(tagline);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);
    }

    private void addRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_BODY);
        val.setForeground(Theme.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        parent.add(row);

        // Divider
        JSeparator div = new JSeparator();
        div.setForeground(Theme.alpha(Theme.BORDER, 80));
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        parent.add(div);
    }

    public static void show(JFrame parent, Transaction tx, String accountNumber, String ownerName) {
        new ReceiptDialog(parent, tx, accountNumber, ownerName);
    }
}
