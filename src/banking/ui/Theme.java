package banking.ui;

import java.awt.*;

public class Theme {
    // === Primary Palette ===
    public static final Color BG_DARK        = new Color(10, 12, 24);
    public static final Color BG_CARD        = new Color(18, 22, 40);
    public static final Color BG_SURFACE     = new Color(24, 28, 52);
    public static final Color BG_INPUT       = new Color(30, 36, 62);
    public static final Color BG_HOVER       = new Color(38, 44, 75);

    public static final Color ACCENT_BLUE    = new Color(64, 130, 255);
    public static final Color ACCENT_PURPLE  = new Color(130, 80, 255);
    public static final Color ACCENT_TEAL    = new Color(0, 200, 180);
    public static final Color ACCENT_PINK    = new Color(240, 80, 160);

    public static final Color SUCCESS        = new Color(52, 211, 153);
    public static final Color DANGER         = new Color(248, 80, 90);
    public static final Color WARNING        = new Color(251, 191, 36);
    public static final Color INFO           = new Color(96, 165, 250);

    public static final Color TEXT_PRIMARY   = new Color(240, 242, 255);
    public static final Color TEXT_SECONDARY = new Color(148, 163, 200);
    public static final Color TEXT_MUTED     = new Color(80, 90, 130);
    public static final Color BORDER         = new Color(40, 48, 80);

    // === Gradients ===
    public static final Color GRAD_START     = new Color(64, 130, 255);
    public static final Color GRAD_END       = new Color(130, 80, 255);

    // === Fonts ===
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
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
        } catch (Exception ignored) {}

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
