package banking.ui;

import java.awt.*;

public class Theme {
    // === Primary Palette (Light Theme) ===
    public static final Color BG_DARK = new Color(245, 247, 250); // page background
    public static final Color BG_CARD = new Color(255, 255, 255); // card / panel
    public static final Color BG_SURFACE = new Color(250, 251, 253); // surface elements
    public static final Color BG_INPUT = new Color(255, 255, 255); // inputs
    public static final Color BG_HOVER = new Color(236, 240, 248); // hover tint

    public static final Color ACCENT_BLUE = new Color(14, 88, 249);
    public static final Color ACCENT_PURPLE = new Color(102, 51, 153);
    public static final Color ACCENT_TEAL = new Color(6, 160, 140);
    public static final Color ACCENT_PINK = new Color(219, 39, 119);

    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color INFO = new Color(59, 130, 246);

    public static final Color TEXT_PRIMARY = new Color(17, 24, 39); // dark text
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105); // secondary
    public static final Color TEXT_MUTED = new Color(107, 114, 128); // muted
    public static final Color BORDER = new Color(226, 232, 240); // light border

    // === Gradients ===
    public static final Color GRAD_START = new Color(64, 130, 255);
    public static final Color GRAD_END = new Color(130, 80, 255);

    // === Fonts ===
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BIG_NUM = new Font("Segoe UI", Font.BOLD, 28);

    public static GradientPaint getButtonGradient(int width) {
        return new GradientPaint(0, 0, GRAD_START, width, 0, GRAD_END);
    }

    public static Color alpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static void setupLookAndFeel() {
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        javax.swing.UIManager.put("OptionPane.background", BG_CARD);
        javax.swing.UIManager.put("Panel.background", BG_CARD);
        javax.swing.UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        javax.swing.UIManager.put("Button.background", BG_SURFACE);
        javax.swing.UIManager.put("Button.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("Button.font", FONT_BODY);
        javax.swing.UIManager.put("Label.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("Label.font", FONT_BODY);
        javax.swing.UIManager.put("ComboBox.background", BG_INPUT);
        javax.swing.UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("ComboBox.font", FONT_BODY);
        javax.swing.UIManager.put("TextField.background", BG_INPUT);
        javax.swing.UIManager.put("TextField.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("TextField.font", FONT_BODY);
        javax.swing.UIManager.put("Table.background", BG_SURFACE);
        javax.swing.UIManager.put("Table.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("Table.font", FONT_BODY);
        javax.swing.UIManager.put("Table.gridColor", BORDER);
        javax.swing.UIManager.put("TableHeader.background", BG_CARD);
        javax.swing.UIManager.put("TableHeader.foreground", ACCENT_BLUE);
        javax.swing.UIManager.put("TableHeader.font", FONT_SUBHEAD);
        javax.swing.UIManager.put("ScrollPane.background", BG_DARK);
        javax.swing.UIManager.put("ScrollBar.background", BG_DARK);
        javax.swing.UIManager.put("ScrollBar.thumb", BG_SURFACE);
        javax.swing.UIManager.put("ScrollBar.thumbShadow", BORDER);
        javax.swing.UIManager.put("ScrollBar.width", 8);
    }
}
