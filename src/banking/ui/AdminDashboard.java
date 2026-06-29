package banking.ui;

import banking.data.DataManager;
import banking.models.*;
import banking.services.*;
import banking.ui.components.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final AuthService authService;
    private final BankingService bankingService;
    private final DataManager dm;

    private JPanel mainContent;
    private CardLayout contentLayout;

    // Tables
    private DefaultTableModel usersModel;
    private DefaultTableModel accountsModel;
    private DefaultTableModel transactionsModel;

    public AdminDashboard(AuthService authService) {
        this.authService = authService;
        this.bankingService = new BankingService();
        this.dm = DataManager.getInstance();

        setTitle("BankPro Admin — Control Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        mainContent = new JPanel();
        contentLayout = new CardLayout();
        mainContent.setLayout(contentLayout);
        mainContent.setBackground(Theme.BG_DARK);

        mainContent.add(buildOverviewPanel(), "OVERVIEW");
        mainContent.add(buildUsersPanel(), "USERS");
        mainContent.add(buildAccountsPanel(), "ACCOUNTS");
        mainContent.add(buildTransactionsPanel(), "TRANSACTIONS");

        add(mainContent, BorderLayout.CENTER);
        contentLayout.show(mainContent, "OVERVIEW");
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(12, 8, 30));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 16, 22, 16));

        JLabel logo = new JLabel("🏦 BankPro");
        logo.setFont(Theme.FONT_HEADING);
        logo.setForeground(Theme.TEXT_PRIMARY);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel("  ADMIN PANEL");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(Theme.ACCENT_PURPLE);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("👑 " + authService.getCurrentUser().getFullName());
        userLabel.setFont(Theme.FONT_SMALL);
        userLabel.setForeground(Theme.ACCENT_TEAL);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        sidebar.add(logo); sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(badge); sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(userLabel); sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sep); sidebar.add(Box.createVerticalStrut(16));

        String[][] navItems = {
            {"📊", "Overview", "OVERVIEW"},
            {"👥", "Manage Users", "USERS"},
            {"💳", "All Accounts", "ACCOUNTS"},
            {"📋", "All Transactions", "TRANSACTIONS"},
        };

        for (String[] item : navItems) {
            sidebar.add(createNavButton(item[0] + "  " + item[1], item[2]));
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        GradientButton logoutBtn = new GradientButton("Sign Out", Theme.DANGER, new Color(180, 40, 60));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.addActionListener(e -> {
            authService.logout();
            dispose();
            new LoginFrame(authService);
        });
        sidebar.add(logoutBtn);
        return sidebar;
    }

    private JButton createNavButton(String text, String panel) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(Theme.BG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(Theme.FONT_BODY);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        btn.addActionListener(e -> {
            contentLayout.show(mainContent, panel);
            if (panel.equals("USERS")) refreshUsersTable();
            else if (panel.equals("ACCOUNTS")) refreshAccountsTable();
            else if (panel.equals("TRANSACTIONS")) refreshTransactionsTable();
        });
        return btn;
    }

    // ===================== OVERVIEW =====================
    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("System Overview");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        List<User> users = dm.getAllUsers();
        List<Account> accounts = bankingService.getAllAccounts();
        List<Transaction> txns = bankingService.getAllTransactions();

        long totalUsers = users.stream().filter(u -> !u.isAdmin()).count();
        long activeAccounts = accounts.stream().filter(Account::isActive).count();
        double totalAssets = accounts.stream().filter(Account::isActive).mapToDouble(Account::getBalance).sum();
        long totalTxns = txns.size();

        JPanel stats = new JPanel(new GridLayout(2, 2, 16, 16));
        stats.setOpaque(false);
        stats.add(buildStatCard("👥 Total Customers", String.valueOf(totalUsers), Theme.ACCENT_BLUE, "Registered users"));
        stats.add(buildStatCard("💳 Active Accounts", String.valueOf(activeAccounts), Theme.ACCENT_PURPLE, "Open accounts"));
        stats.add(buildStatCard("💰 Total Assets", String.format("$%,.2f", totalAssets), Theme.SUCCESS, "All deposits"));
        stats.add(buildStatCard("📋 Total Transactions", String.valueOf(totalTxns), Theme.ACCENT_TEAL, "All time"));

        // Recent activity
        JLabel recentTitle = new JLabel("Recent Activity");
        recentTitle.setFont(Theme.FONT_HEADING);
        recentTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"Time", "User", "Type", "Amount"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);

        List<Transaction> recent = txns.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(8).toList();

        for (Transaction tx : recent) {
            User u = dm.findUserById(tx.getUserId()).orElse(null);
            model.addRow(new Object[]{
                tx.getFormattedTimestamp(),
                u != null ? u.getFullName() : "Unknown",
                tx.getType().name().replace("_", " "),
                tx.getFormattedAmount()
            });
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.add(recentTitle, BorderLayout.NORTH);
        bottom.add(scroll, BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(stats, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStatCard(String label, String value, Color accent, String sub) {
        CardPanel card = CardPanel.withGradientTop(accent);
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SUBHEAD);
        lbl.setForeground(accent);

        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_BIG_NUM);
        val.setForeground(Theme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.TEXT_MUTED);

        card.add(lbl); card.add(Box.createVerticalStrut(8));
        card.add(val); card.add(Box.createVerticalStrut(4)); card.add(subLbl);
        return card;
    }

    // ===================== USERS PANEL =====================
    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Manage Users");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        // Add user area
        CardPanel addCard = new CardPanel();
        addCard.setBackground(Theme.BG_CARD);
        addCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addCard.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        StyledTextField nameField = new StyledTextField("Full Name");
        nameField.setPreferredSize(new Dimension(130, 36));
        StyledTextField uField = new StyledTextField("Username");
        uField.setPreferredSize(new Dimension(120, 36));
        StyledPasswordField pField = new StyledPasswordField("Password");
        pField.setPreferredSize(new Dimension(120, 36));
        StyledTextField eField = new StyledTextField("Email");
        eField.setPreferredSize(new Dimension(150, 36));
        StyledTextField phField = new StyledTextField("Phone");
        phField.setPreferredSize(new Dimension(120, 36));

        GradientButton addBtn = new GradientButton("+ Add Customer", Theme.ACCENT_TEAL, Theme.ACCENT_BLUE);
        addBtn.setPreferredSize(new Dimension(150, 36));
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim(), u = uField.getText().trim();
            String p = new String(pField.getPassword()), em = eField.getText().trim(), ph = phField.getText().trim();
            if (name.isEmpty() || u.isEmpty() || p.isEmpty()) {
                showMsg("Name, username, and password required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = authService.register(u, p, name, em, ph);
            if (ok) {
                showMsg("Customer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText(""); uField.setText(""); pField.setText("");
                eField.setText(""); phField.setText("");
                refreshUsersTable();
            } else {
                showMsg("Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addCard.add(new JLabel("Add:") {{ setForeground(Theme.TEXT_SECONDARY); setFont(Theme.FONT_SUBHEAD); }});
        addCard.add(nameField); addCard.add(uField); addCard.add(pField);
        addCard.add(eField); addCard.add(phField); addCard.add(addBtn);

        // Table
        String[] cols = {"ID", "Full Name", "Username", "Email", "Phone", "Role", "Status", "Created"};
        usersModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(usersModel);

        JButton deleteBtn = new JButton("🗑 Delete Selected");
        deleteBtn.setBackground(Theme.DANGER);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(Theme.FONT_BODY);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showMsg("Select a user to delete.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            String uid = (String) usersModel.getValueAt(row, 0);
            String role = (String) usersModel.getValueAt(row, 5);
            if ("ADMIN".equals(role)) { showMsg("Cannot delete admin accounts.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            int conf = JOptionPane.showConfirmDialog(this, "Delete this user and all their data?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf == JOptionPane.YES_OPTION) { dm.deleteUser(uid); refreshUsersTable(); }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_DARK); scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false); btnRow.add(deleteBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(addCard, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);
        tablePanel.add(btnRow, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);

        refreshUsersTable();
        return panel;
    }

    private void refreshUsersTable() {
        if (usersModel == null) return;
        usersModel.setRowCount(0);
        for (User u : dm.getAllUsers()) {
            usersModel.addRow(new Object[]{
                u.getId(), u.getFullName(), u.getUsername(), u.getEmail(),
                u.getPhone(), u.getRole(), u.isActive() ? "Active" : "Inactive",
                u.getCreatedAt().toLocalDate()
            });
        }
    }

    // ===================== ACCOUNTS PANEL =====================
    private JPanel buildAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("All Accounts");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"Account No", "Owner", "Type", "Balance", "Status", "Opened"};
        accountsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(accountsModel);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_DARK); scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        GradientButton refreshBtn = new GradientButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshAccountsTable());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false); top.add(title, BorderLayout.WEST); top.add(refreshBtn, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        refreshAccountsTable();
        return panel;
    }

    private void refreshAccountsTable() {
        if (accountsModel == null) return;
        accountsModel.setRowCount(0);
        for (Account acc : bankingService.getAllAccounts()) {
            User u = dm.findUserById(acc.getUserId()).orElse(null);
            accountsModel.addRow(new Object[]{
                acc.getAccountNumber(),
                u != null ? u.getFullName() : "Unknown",
                acc.getTypeDisplay(), acc.getFormattedBalance(),
                acc.isActive() ? "Active" : "Closed",
                acc.getCreatedAt().toLocalDate()
            });
        }
    }

    // ===================== TRANSACTIONS PANEL =====================
    private JPanel buildTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("All Transactions");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"Date & Time", "Account No", "Customer", "Type", "Amount", "Balance After", "Description"};
        transactionsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(transactionsModel);
        // Color amounts
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBackground(row % 2 == 0 ? Theme.BG_SURFACE : Theme.BG_CARD);
                setForeground(Theme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (col == 4 && value != null) {
                    String v = value.toString();
                    setForeground(v.startsWith("+") ? Theme.SUCCESS : Theme.DANGER);
                    setFont(Theme.FONT_SUBHEAD);
                }
                if (isSelected) setBackground(Theme.BG_HOVER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_DARK); scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        GradientButton refreshBtn = new GradientButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshTransactionsTable());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false); top.add(title, BorderLayout.WEST); top.add(refreshBtn, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        refreshTransactionsTable();
        return panel;
    }

    private void refreshTransactionsTable() {
        if (transactionsModel == null) return;
        transactionsModel.setRowCount(0);
        List<Transaction> txns = bankingService.getAllTransactions().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();

        for (Transaction tx : txns) {
            Account acc = dm.findAccountById(tx.getAccountId()).orElse(null);
            User u = dm.findUserById(tx.getUserId()).orElse(null);
            transactionsModel.addRow(new Object[]{
                tx.getFormattedTimestamp(),
                acc != null ? acc.getAccountNumber() : "N/A",
                u != null ? u.getFullName() : "Unknown",
                tx.getType().name().replace("_", " "),
                tx.getFormattedAmount(),
                String.format("$%,.2f", tx.getBalanceAfter()),
                tx.getDescription()
            });
        }
    }

    // ===================== HELPERS =====================
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(Theme.BG_SURFACE);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.BG_HOVER);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setBackground(Theme.BG_CARD);
        table.getTableHeader().setForeground(Theme.ACCENT_BLUE);
        table.getTableHeader().setFont(Theme.FONT_SUBHEAD);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBackground(row % 2 == 0 ? Theme.BG_SURFACE : Theme.BG_CARD);
                setForeground(Theme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (isSelected) setBackground(Theme.BG_HOVER);
                return c;
            }
        });
        return table;
    }

    private void showMsg(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }
}
