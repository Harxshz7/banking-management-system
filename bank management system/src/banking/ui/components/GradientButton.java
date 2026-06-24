package banking.ui.components;

import banking.ui.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class GradientButton extends JButton {
    private Color colorStart;
    private Color colorEnd;
    private boolean hovered = false;
    private boolean pressed = false;

    public GradientButton(String text) {
        this(text, Theme.GRAD_START, Theme.GRAD_END);
    }

    public GradientButton(String text, Color start, Color end) {
        super(text);
        this.colorStart = start;
        this.colorEnd = end;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(Theme.FONT_SUBHEAD);
        setForeground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width, 42));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
            public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w, h, 12, 12);

        Color s = colorStart, e = colorEnd;
        if (pressed) {
            s = s.darker();
            e = e.darker();
        } else if (hovered) {
            s = s.brighter();
            e = e.brighter();
        }

        if (isEnabled()) {
            GradientPaint gp = new GradientPaint(0, 0, s, w, 0, e);
            g2.setPaint(gp);
        } else {
            g2.setColor(Theme.BG_HOVER);
        }
        g2.fill(shape);

        // Subtle glow on hover
        if (hovered && isEnabled()) {
            g2.setColor(Theme.alpha(colorStart, 60));
            g2.setStroke(new BasicStroke(3));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 12, 12));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
