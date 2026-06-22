package banking.ui;

import banking.models.*;
import banking.services.*;
import banking.ui.components.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private final AuthService authService;
    private final BankingService bankingService;
    private final User currentUser;

    private JPanel mainContent;
    private CardLayout contentLayout;
    private JLabel totalBalanceLabel;

    public CustomerDashboard(AuthService authService) {
        this.authService = authService;
        this.bankingService = new BankingService();
        this.currentUser = authService.getCurrentUser();

        setTitle("BankPro — " + currentUser.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildSidebar(), BorderLayout.WEST);

        mainContent = new JPanel();
        contentLayout = new CardLayout();
        mainContent.setLayout(contentLayout);
        mainContent.setBackground(Theme.BG_DARK);

        mainContent.add(buildDashboardPanel(), "DASHBOARD");
        mainContent.add(buildAccountsPanel(), "ACCOUNTS");
        mainContent.add(buildDepositPanel(), "DEPOSIT");
        mainContent.add(buildWithdrawPanel(), "WITHDRAW");
        mainContent.add(buildTransferPanel(), "TRANSFER");
        mainContent.add(buildHistoryPanel(), "HISTORY");

        add(mainContent, BorderLayout.CENTER);
        contentLayout.show(mainContent, "DASHBOARD");
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Theme.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        // Logo
        JLabel logo = new JLabel("🏦 BankPro");
        logo.setFont(Theme.FONT_HEADING);
        logo.setForeground(Theme.TEXT_PRIMARY);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("👤 " + currentUser.getFullName());
        userLabel.setFont(Theme.FONT_SMALL);
        userLabel.setForeground(Theme.ACCENT_TEAL);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        sidebar.add(logo); sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(userLabel); sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sep); sidebar.add(Box.createVerticalStrut(16));

        String[][] navItems = {
            {"🏠", "Dashboard", "DASHBOARD"},
            {"💳", "My Accounts", "ACCOUNTS"},
            {"💰", "Deposit", "DEPOSIT"},
            {"🏧", "Withdraw", "WITHDRAW"},
            {"↔️", "Transfer", "TRANSFER"},
            {"📋", "History", "HISTORY"},
        };

        for (String[] item : navItems) {
            sidebar.add(createNavButton(item[0] + "  " + item[1], item[2]));
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        GradientButton logoutBtn = new GradientButton("Sign Out", Theme.DANGER, new Color(180, 40, 60));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.addActionListener(e -> logout());
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
            if (panel.equals("ACCOUNTS")) refreshAccountsPanel();
            else if (panel.equals("HISTORY")) refreshHistoryPanel();
            else if (panel.equals("DASHBOARD")) refreshDashboard();
            else if (panel.equals("DEPOSIT") || panel.equals("WITHDRAW") || panel.equals("TRANSFER"))
                refreshTransactionPanels();
        });
        return btn;
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // Header
        JLabel title = new JLabel("Welcome back, " + currentUser.getFullName() + "! 👋");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Here's your financial overview");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(title); header.add(Box.createVerticalStrut(4)); header.add(sub);

        // Stats
        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);

        List<Account> accounts = bankingService.getUserAccounts(currentUser.getId());
        double totalBalance = accounts.stream().filter(Account::isActive).mapToDouble(Account::getBalance).sum();
        long activeAccounts = accounts.stream().filter(Account::isActive).count();
        long totalTxns = bankingService.getUserTransactions(currentUser.getId()).size();

        stats.add(buildStatCard("💰 Total Balance",
                String.format("$%,.2f", totalBalance), Theme.ACCENT_BLUE, "All accounts combined"));
        stats.add(buildStatCard("💳 Active Accounts",
                String.valueOf(activeAccounts), Theme.ACCENT_PURPLE, "Open accounts"));
        stats.add(buildStatCard("📋 Transactions",
                String.valueOf(totalTxns), Theme.ACCENT_TEAL, "All time"));

        // Recent accounts
        JLabel accTitle = new JLabel("Your Accounts");
        accTitle.setFont(Theme.FONT_HEADING);
        accTitle.setForeground(Theme.TEXT_PRIMARY);

        JPanel accountCards = new JPanel(new GridLayout(0, 2, 14, 14));
        accountCards.setOpaque(false);

        for (Account acc : accounts) {
            if (acc.isActive()) {
                accountCards.add(buildAccountCard(acc));
            }
        }

        if (accounts.isEmpty()) {
            JLabel empty = new JLabel("No accounts yet. Create one in 'My Accounts'.");
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setFont(Theme.FONT_BODY);
            accountCards.add(empty);
        }

        JPanel bottomSection = new JPanel(new BorderLayout(0, 12));
        bottomSection.setOpaque(false);
        bottomSection.add(accTitle, BorderLayout.NORTH);
        bottomSection.add(new JScrollPane(accountCards) {{
            setOpaque(false); getViewport().setOpaque(false);
            setBorder(null); setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        }}, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(stats, BorderLayout.CENTER);
        panel.add(bottomSection, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshDashboard() {
        mainContent.remove(mainContent.getComponent(0));
        mainContent.add(buildDashboardPanel(), "DASHBOARD", 0);
        contentLayout.show(mainContent, "DASHBOARD");
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

    private JPanel buildAccountCard(Account acc) {
        CardPanel card = new CardPanel(Theme.alpha(Theme.ACCENT_BLUE, 60), 14, true);
        card.setBackground(Theme.BG_SURFACE);
        card.setLayout(new BorderLayout(8, 4));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel type = new JLabel(acc.getTypeDisplay());
        type.setFont(Theme.FONT_SUBHEAD);
        type.setForeground(Theme.ACCENT_BLUE);

        JLabel num = new JLabel("●●●● " + acc.getAccountNumber().substring(6));
        num.setFont(Theme.FONT_MONO);
        num.setForeground(Theme.TEXT_SECONDARY);

        JLabel balance = new JLabel(acc.getFormattedBalance());
        balance.setFont(Theme.FONT_BIG_NUM);
        balance.setForeground(Theme.TEXT_PRIMARY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(type, BorderLayout.WEST);
        top.add(num, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(balance, BorderLayout.CENTER);
        return card;
    }

    // ===================== ACCOUNTS PANEL =====================

    private JPanel accountsPanel;
    private JPanel accountCardsContainer;

    private JPanel buildAccountsPanel() {
        accountsPanel = new JPanel(new BorderLayout(0, 16));
        accountsPanel.setBackground(Theme.BG_DARK);
        accountsPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("My Accounts");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        // Create account section
        CardPanel createCard = new CardPanel();
        createCard.setBackground(Theme.BG_CARD);
        createCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        createCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel createLabel = new JLabel("Open New Account:");
        createLabel.setFont(Theme.FONT_SUBHEAD);
        createLabel.setForeground(Theme.TEXT_SECONDARY);

        JComboBox<Account.AccountType> typeBox = new JComboBox<>(Account.AccountType.values());
        typeBox.setBackground(Theme.BG_INPUT);
        typeBox.setForeground(Theme.TEXT_PRIMARY);
        typeBox.setFont(Theme.FONT_BODY);
        typeBox.setPreferredSize(new Dimension(160, 36));

        StyledTextField initDepField = new StyledTextField("Initial Deposit");
        initDepField.setPreferredSize(new Dimension(140, 36));

        GradientButton createBtn = new GradientButton("+ Open Account", Theme.ACCENT_TEAL, Theme.ACCENT_BLUE);
        createBtn.setPreferredSize(new Dimension(150, 36));
        createBtn.addActionListener(e -> {
            try {
                double dep = Double.parseDouble(initDepField.getText().trim());
                Account acc = bankingService.createAccount(currentUser.getId(),
                        (Account.AccountType) typeBox.getSelectedItem(), dep);
                if (acc != null) {
                    JOptionPane.showMessageDialog(this, "Account created!\nAccount No: " + acc.getAccountNumber(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    initDepField.setText("");
                    refreshAccountsPanel();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid deposit amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        createCard.add(createLabel); createCard.add(typeBox);
        createCard.add(initDepField); createCard.add(createBtn);

        accountCardsContainer = new JPanel(new GridLayout(0, 2, 14, 14));
        accountCardsContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(accountCardsContainer);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        accountsPanel.add(header, BorderLayout.NORTH);
        accountsPanel.add(createCard, BorderLayout.CENTER);
        accountsPanel.add(scroll, BorderLayout.SOUTH);

        refreshAccountsPanel();
        return accountsPanel;
    }

    private void refreshAccountsPanel() {
        if (accountCardsContainer == null) return;
        accountCardsContainer.removeAll();
        List<Account> accounts = bankingService.getUserAccounts(currentUser.getId());
        for (Account acc : accounts) {
            if (acc.isActive()) {
                accountCardsContainer.add(buildDetailedAccountCard(acc));
            }
        }
        if (accountCardsContainer.getComponentCount() == 0) {
            JLabel empty = new JLabel("No active accounts. Open one above!");
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setFont(Theme.FONT_BODY);
            accountCardsContainer.add(empty);
        }
        accountCardsContainer.revalidate();
        accountCardsContainer.repaint();
    }

    private JPanel buildDetailedAccountCard(Account acc) {
        CardPanel card = new CardPanel(Theme.BORDER, 14, true);
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));

        JLabel type = new JLabel(acc.getTypeDisplay() + " Account");
        type.setFont(Theme.FONT_HEADING);
        type.setForeground(Theme.TEXT_PRIMARY);

        JLabel num = new JLabel("Account No: " + acc.getAccountNumber());
        num.setFont(Theme.FONT_MONO);
        num.setForeground(Theme.TEXT_SECONDARY);

        JLabel bal = new JLabel(acc.getFormattedBalance());
        bal.setFont(new Font("Segoe UI", Font.BOLD, 32));
        bal.setForeground(Theme.SUCCESS);

        JLabel since = new JLabel("Opened: " + acc.getCreatedAt().toLocalDate());
        since.setFont(Theme.FONT_SMALL);
        since.setForeground(Theme.TEXT_MUTED);

        GradientButton closeBtn = new GradientButton("Close Account", Theme.DANGER, new Color(180, 40, 60));
        closeBtn.setPreferredSize(new Dimension(150, 32));
        closeBtn.setFont(Theme.FONT_SMALL);
        closeBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Close this account? Remaining balance will be forfeited.", "Confirm",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                bankingService.closeAccount(acc.getId(), currentUser.getId());
                refreshAccountsPanel();
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(type, BorderLayout.WEST);

        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setOpaque(false);
        mid.add(num); mid.add(Box.createVerticalStrut(8)); mid.add(bal);
        mid.add(Box.createVerticalStrut(4)); mid.add(since);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(closeBtn);

        card.add(top, BorderLayout.NORTH);
        card.add(mid, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ===================== DEPOSIT PANEL =====================
    private JComboBox<String> depositAccBox;
    private StyledTextField depositAmount;
    private StyledTextField depositNote;

    private JPanel buildDepositPanel() {
        JPanel panel = makeTransactionPanel("💰 Deposit Funds", Theme.SUCCESS);

        CardPanel card = new CardPanel();
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        depositAccBox = new JComboBox<>();
        depositAccBox.setBackground(Theme.BG_INPUT);
        depositAccBox.setForeground(Theme.TEXT_PRIMARY);
        depositAccBox.setFont(Theme.FONT_BODY);

        depositAmount = new StyledTextField("Enter amount (e.g. 1000)");
        depositNote = new StyledTextField("Note (optional)");

        GradientButton btn = new GradientButton("Deposit Funds", Theme.SUCCESS, Theme.ACCENT_TEAL);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addActionListener(e -> {
            try {
                String sel = (String) depositAccBox.getSelectedItem();
                if (sel == null) { showError("No account selected."); return; }
                String accId = getAccountIdFromLabel(sel);
                double amt = Double.parseDouble(depositAmount.getText().trim());
                boolean ok = bankingService.deposit(accId, currentUser.getId(), amt, depositNote.getText().trim());
                if (ok) {
                    JOptionPane.showMessageDialog(this, String.format("✅ $%.2f deposited successfully!", amt),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    depositAmount.setText(""); depositNote.setText("");
                    refreshTransactionPanels();
                } else showError("Deposit failed. Check amount.");
            } catch (NumberFormatException ex) { showError("Enter a valid amount."); }
        });

        card.add(makeLabel("Select Account")); card.add(Box.createVerticalStrut(6));
        card.add(depositAccBox); card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Amount ($)")); card.add(Box.createVerticalStrut(6));
        card.add(depositAmount); card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Note")); card.add(Box.createVerticalStrut(6));
        card.add(depositNote); card.add(Box.createVerticalStrut(24));
        card.add(btn);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // ===================== WITHDRAW PANEL =====================
    private JComboBox<String> withdrawAccBox;
    private StyledTextField withdrawAmount;
    private StyledTextField withdrawNote;

    private JPanel buildWithdrawPanel() {
        JPanel panel = makeTransactionPanel("🏧 Withdraw Funds", Theme.WARNING);

        CardPanel card = new CardPanel();
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        withdrawAccBox = new JComboBox<>();
        withdrawAccBox.setBackground(Theme.BG_INPUT);
        withdrawAccBox.setForeground(Theme.TEXT_PRIMARY);
        withdrawAccBox.setFont(Theme.FONT_BODY);

        withdrawAmount = new StyledTextField("Enter amount");
        withdrawNote = new StyledTextField("Note (optional)");

        GradientButton btn = new GradientButton("Withdraw Funds", Theme.WARNING, new Color(200, 120, 0));
        btn.setForeground(Theme.BG_DARK);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addActionListener(e -> {
            try {
                String sel = (String) withdrawAccBox.getSelectedItem();
                if (sel == null) { showError("No account selected."); return; }
                String accId = getAccountIdFromLabel(sel);
                double amt = Double.parseDouble(withdrawAmount.getText().trim());
                boolean ok = bankingService.withdraw(accId, currentUser.getId(), amt, withdrawNote.getText().trim());
                if (ok) {
                    JOptionPane.showMessageDialog(this, String.format("✅ $%.2f withdrawn successfully!", amt),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    withdrawAmount.setText(""); withdrawNote.setText("");
                    refreshTransactionPanels();
                } else showError("Insufficient balance or invalid amount.");
            } catch (NumberFormatException ex) { showError("Enter a valid amount."); }
        });

        card.add(makeLabel("Select Account")); card.add(Box.createVerticalStrut(6));
        card.add(withdrawAccBox); card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Amount ($)")); card.add(Box.createVerticalStrut(6));
        card.add(withdrawAmount); card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Note")); card.add(Box.createVerticalStrut(6));
        card.add(withdrawNote); card.add(Box.createVerticalStrut(24));
        card.add(btn);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // ===================== TRANSFER PANEL =====================
    private JComboBox<String> transferFromBox;
    private StyledTextField transferToNumber;
    private StyledTextField transferAmount;
    private StyledTextField transferNote;

    private JPanel buildTransferPanel() {
        JPanel panel = makeTransactionPanel("↔️ Transfer Funds", Theme.ACCENT_BLUE);

        CardPanel card = new CardPanel();
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        transferFromBox = new JComboBox<>();
        transferFromBox.setBackground(Theme.BG_INPUT);
        transferFromBox.setForeground(Theme.TEXT_PRIMARY);
        transferFromBox.setFont(Theme.FONT_BODY);

        transferToNumber = new StyledTextField("Recipient Account Number");
        transferAmount = new StyledTextField("Amount to Transfer");
        transferNote = new StyledTextField("Transfer Note (optional)");

        GradientButton btn = new GradientButton("Send Transfer");
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addActionListener(e -> {
            try {
                String sel = (String) transferFromBox.getSelectedItem();
                if (sel == null) { showError("No source account selected."); return; }
                String accId = getAccountIdFromLabel(sel);
                String toNumber = transferToNumber.getText().trim();
                double amt = Double.parseDouble(transferAmount.getText().trim());
                String err = bankingService.transfer(accId, toNumber, currentUser.getId(), amt,
                        transferNote.getText().trim());
                if (err == null) {
                    JOptionPane.showMessageDialog(this, String.format("✅ $%.2f transferred successfully!", amt),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    transferToNumber.setText(""); transferAmount.setText(""); transferNote.setText("");
                    refreshTransactionPanels();
                } else showError(err);
            } catch (NumberFormatException ex) { showError("Enter a valid amount."); }
        });

        card.add(makeLabel("From Account")); card.add(Box.createVerticalStrut(6));
        card.add(transferFromBox); card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("To Account Number")); card.add(Box.createVerticalStrut(6));
        card.add(transferToNumber); card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Amount ($)")); card.add(Box.createVerticalStrut(6));
        card.add(transferAmount); card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Note")); card.add(Box.createVerticalStrut(6));
        card.add(transferNote); card.add(Box.createVerticalStrut(24));
        card.add(btn);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void refreshTransactionPanels() {
        List<Account> accounts = bankingService.getUserAccounts(currentUser.getId());
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Account acc : accounts) {
            if (acc.isActive()) {
                model.addElement(acc.getAccountNumber() + " - " + acc.getTypeDisplay() +
                        " (" + acc.getFormattedBalance() + ") [" + acc.getId() + "]");
            }
        }
        if (depositAccBox != null) depositAccBox.setModel(new DefaultComboBoxModel<>(getAccountLabels(accounts)));
        if (withdrawAccBox != null) withdrawAccBox.setModel(new DefaultComboBoxModel<>(getAccountLabels(accounts)));
        if (transferFromBox != null) transferFromBox.setModel(new DefaultComboBoxModel<>(getAccountLabels(accounts)));
    }

    private String[] getAccountLabels(List<Account> accounts) {
        return accounts.stream().filter(Account::isActive)
                .map(a -> a.getAccountNumber() + " - " + a.getTypeDisplay() +
                        " (" + a.getFormattedBalance() + ") [" + a.getId() + "]")
                .toArray(String[]::new);
    }

    private String getAccountIdFromLabel(String label) {
        return label.substring(label.lastIndexOf('[') + 1, label.lastIndexOf(']'));
    }

    // ===================== HISTORY PANEL =====================
    private JTable historyTable;
    private DefaultTableModel historyModel;

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Transaction History");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"Date & Time", "Account No", "Type", "Amount", "Balance After", "Description"};
        historyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(historyModel);
        historyTable.setBackground(Theme.BG_SURFACE);
        historyTable.setForeground(Theme.TEXT_PRIMARY);
        historyTable.setFont(Theme.FONT_BODY);
        historyTable.setRowHeight(34);
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        historyTable.setSelectionBackground(Theme.BG_HOVER);
        historyTable.setSelectionForeground(Theme.TEXT_PRIMARY);
        historyTable.getTableHeader().setBackground(Theme.BG_CARD);
        historyTable.getTableHeader().setForeground(Theme.ACCENT_BLUE);
        historyTable.getTableHeader().setFont(Theme.FONT_SUBHEAD);
        historyTable.getTableHeader().setReorderingAllowed(false);

        // Custom cell renderer for amount coloring
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBackground(row % 2 == 0 ? Theme.BG_SURFACE : Theme.BG_CARD);
                setForeground(Theme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (col == 3 && value != null) {
                    String v = value.toString();
                    setForeground(v.startsWith("+") ? Theme.SUCCESS : Theme.DANGER);
                    setFont(Theme.FONT_SUBHEAD);
                }
                if (isSelected) setBackground(Theme.BG_HOVER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        refreshHistoryPanel();
        return panel;
    }

    private void refreshHistoryPanel() {
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        List<Transaction> txns = bankingService.getUserTransactions(currentUser.getId());
        List<Account> accounts = bankingService.getUserAccounts(currentUser.getId());

        for (Transaction tx : txns) {
            String accNum = accounts.stream()
                    .filter(a -> a.getId().equals(tx.getAccountId()))
                    .map(Account::getAccountNumber)
                    .findFirst().orElse("N/A");
            historyModel.addRow(new Object[]{
                tx.getFormattedTimestamp(), accNum,
                tx.getType().name().replace("_", " "),
                tx.getFormattedAmount(),
                String.format("$%,.2f", tx.getBalanceAfter()),
                tx.getDescription()
            });
        }
    }

    // ===================== HELPERS =====================

    private JPanel makeTransactionPanel(String title, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 120, 28, 120));

        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.FONT_TITLE);
        lbl.setForeground(accent);
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void logout() {
        authService.logout();
        dispose();
        new LoginFrame(authService);
    }
}
