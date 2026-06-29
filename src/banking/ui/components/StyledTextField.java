package banking.ui.components;

import banking.ui.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class StyledTextField extends JTextField {
    private String placeholder;
    private boolean focused = false;

    public StyledTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT_PRIMARY);
        setCaretColor(Theme.ACCENT_BLUE);
        setBackground(Theme.BG_INPUT);
        setPreferredSize(new Dimension(getPreferredSize().width, 42));

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { focused = true; repaint(); }
            public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(Theme.BG_INPUT);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

        // Border
        Color borderColor = focused ? Theme.ACCENT_BLUE : Theme.BORDER;
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
        g2.dispose();

        super.paintComponent(g);

        // Placeholder
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D pg = (Graphics2D) g.create();
            pg.setFont(Theme.FONT_BODY);
            pg.setColor(Theme.TEXT_MUTED);
            Insets ins = getInsets();
            pg.drawString(placeholder, ins.left, getHeight() / 2 + 5);
            pg.dispose();
        }
    }

    @Override
    public void paintBorder(Graphics g) { /* Handled in paintComponent */ }
}
