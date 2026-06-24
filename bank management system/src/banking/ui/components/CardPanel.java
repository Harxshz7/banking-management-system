package banking.ui.components;

import banking.ui.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CardPanel extends JPanel {
    private Color borderColor;
    private int radius;
    private boolean hasShadow;

    public CardPanel() {
        this(Theme.BORDER, 16, true);
    }

    public CardPanel(Color borderColor, int radius, boolean hasShadow) {
        this.borderColor = borderColor;
        this.radius = radius;
        this.hasShadow = hasShadow;
        setOpaque(false);
        setBackground(Theme.BG_CARD);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 0, y = 0, w = getWidth(), h = getHeight();

        // Shadow
        if (hasShadow) {
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fill(new RoundRectangle2D.Float(x + i, y + i, w - i, h - i, radius, radius));
            }
        }

        // Background
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(x, y, w - 6, h - 6, radius, radius));

        // Border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 7, h - 7, radius, radius));

        g2.dispose();
    }

    public void setAccentBorder(Color color) {
        this.borderColor = color;
        repaint();
    }

    public static CardPanel withGradientTop(Color accentColor) {
        CardPanel p = new CardPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                GradientPaint gp = new GradientPaint(0, 0, Theme.alpha(accentColor, 80), w, 0,
                        Theme.alpha(accentColor, 10));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 6, 4, 4, 4);
                g2.dispose();
            }
        };
        return p;
    }
}
