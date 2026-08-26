package banking.ui;

import banking.models.*;
import banking.services.*;
import banking.ui.components.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private final AuthService authService;
    private final BankingService bankingService;
    private final LoanService loanService;
    private final BankingServiceExtensions extService;

    private JPanel mainContent;
    private CardLayout contentLayout;
    private JLabel totalBalanceLabel;

    public CustomerDashboard(AuthService authService) {
        this.authService = authService;
        this.bankingService = new BankingService();
        this.loanService = new LoanService();
        this.extService = new BankingServiceExtensions();
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
        mainContent.add(buildLoansPanel(), "LOANS");
        mainContent.add(buildBeneficiariesPanel(), "BENEFICIARIES");
        mainContent.add(buildScheduledPanel(), "SCHEDULED");

        add(mainContent, BorderLayout.CENTER);
        contentLayout.show(mainContent, "DASHBOARD");
        setVisible(true);
    }

    // ===================== SIDEBAR =====================

    private final User currentUser;

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Theme.ACCENT_NAVY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.alpha(Color.WHITE, 20));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(true);
        sidebar.setBackground(Theme.ACCENT_NAVY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel logo = new JLabel("\uD83C\uDFE6 BankPro");
        logo.setFont(Theme.FONT_HEADING);
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("\uD83D\uDC64 " + currentUser.getFullName());
        userLabel.setFont(Theme.FONT_SMALL);
        userLabel.setForeground(Theme.BG_SURFACE);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(userLabel);
        sidebar.add(Box.createVerticalStrut(18));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(18));

        String[][] navItems = {
                { "\uD83C\uDFE0", "Dashboard", "DASHBOARD" },
                { "\uD83D\uDCB3", "My Accounts", "ACCOUNTS" },
                { "\uD83D\uDCB0", "Deposit", "DEPOSIT" },
                { "\uD83C\uDF7F", "Withdraw", "WITHDRAW" },
                { "\u2194\uFE0F", "Transfer", "TRANSFER" },
                { "\uD83D\uDCCB", "History", "HISTORY" },
                { "\uD83C\uDF9E", "Loans", "LOANS" },
                { "\uD83D\uDC65", "Beneficiaries", "BENEFICIARIES" },
                { "\uD83D\uDCC5", "Scheduled", "SCHEDULED" },
        };

        for (String[] item : navItems) {
            sidebar.add(createNavButton(item[0] + "  " + item[1], item[2]));
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        GradientButton logoutBtn = new GradientButton("Sign Out", Theme.DANGER, new Color(180, 40, 60));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setForeground(Color.WHITE);
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
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(Theme.FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.addActionListener(e -> {
            contentLayout.show(mainContent, panel);
            if (panel.equals("ACCOUNTS"))
                refreshAccountsPanel();
            else if (panel.equals("HISTORY"))
                refreshHistoryPanel();
            else if (panel.equals("DASHBOARD"))
                refreshDashboard();
            else if (panel.equals("DEPOSIT") || panel.equals("WITHDRAW") || panel.equals("TRANSFER"))
                refreshTransactionPanels();
            else if (panel.equals("LOANS"))
                refreshLoansPanel();
            else if (panel.equals("BENEFICIARIES"))
                refreshBeneficiariesPanel();
            else if (panel.equals("SCHEDULED"))
                refreshScheduledPanel();
        });
        return btn;
    }

    // ===================== PIN VERIFICATION HELPER =====================

    private boolean verifyPin() {
        return PinDialog.verify(this, currentUser);
    }

    // ===================== RECEIPT HELPER =====================

    private void showReceipt(Transaction tx, String accNumber) {
        if (tx != null) {
            ReceiptDialog.show(this, tx, accNumber, currentUser.getFullName());
        }
    }

    // ===================== DASHBOARD PANEL =====================

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(Theme.BG_SUBTLE);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Welcome back, " + currentUser.getFullName() + "! \uD83D\uDC4B");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_NAVY);

        JLabel sub = new JLabel("Your finances at a glance.");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);

        List<Account> accounts = bankingService.getUserAccounts(currentUser.getId());
        double totalBalance = accounts.stream().filter(Account::isActive).mapToDouble(Account::getBalance).sum();
        long activeAccounts = accounts.stream().filter(Account::isActive).count();
        long totalTxns = bankingService.getUserTransactions(currentUser.getId()).size();

        stats.add(buildStatCard("\uD83D\uDCB0 Total Balance",
                String.format("$%,.2f", totalBalance), Theme.ACCENT_BLUE, "All accounts combined"));
        stats.add(buildStatCard("\uD83D\uDCB3 Active Accounts",
                String.valueOf(activeAccounts), Theme.ACCENT_ORANGE, "Open accounts"));
        stats.add(buildStatCard("\uD83D\uDCCB Transactions",
                String.valueOf(totalTxns), Theme.ACCENT_TEAL, "All time"));

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
        bottomSection.add(new JScrollPane(accountCards) {
            {
                setOpaque(false);
                getViewport().setOpaque(false);
                setBorder(null);
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
            }
        }, BorderLayout.CENTER);

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

        card.add(lbl);
        card.add(Box.createVerticalStrut(8));
        card.add(val);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);
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

        JLabel num = new JLabel("\u25CF\u25CF\u25CF\u25CF " + acc.getAccountNumber().substring(6));
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
                JOptionPane.showMessageDialog(this, "Enter a valid deposit amount.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        createCard.add(createLabel);
        createCard.add(typeBox);
        createCard.add(initDepField);
        createCard.add(createBtn);

        accountCardsContainer = new JPanel(new GridLayout(0, 2, 14, 14));
        accountCardsContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(accountCardsContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
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
        if (accountCardsContainer == null)
            return;
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
        mid.add(num);
        mid.add(Box.createVerticalStrut(8));
        mid.add(bal);
        mid.add(Box.createVerticalStrut(4));
        mid.add(since);

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
        JPanel panel = makeTransactionPanel("\uD83D\uDCB0 Deposit Funds", Theme.SUCCESS);

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
                if (sel == null) {
                    showError("No account selected.");
                    return;
                }
                String accId = getAccountIdFromLabel(sel);
                double amt = Double.parseDouble(depositAmount.getText().trim());
                BankingService.TransactionResult res = bankingService.deposit(
                        accId, currentUser.getId(), amt, depositNote.getText().trim(), "ONLINE");
                if (res != null && res.isSuccess()) {
                    depositAmount.setText("");
                    depositNote.setText("");
                    refreshTransactionPanels();
                    showReceipt(res.getTransaction(), getAccountNumberFromId(accId));
                } else
                    showError(res != null && res.getErrorMessage() != null ? res.getErrorMessage()
                            : "Deposit failed. Check amount.");
            } catch (NumberFormatException ex) {
                showError("Enter a valid amount.");
            }
        });

        card.add(makeLabel("Select Account"));
        card.add(Box.createVerticalStrut(6));
        card.add(depositAccBox);
        card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Amount ($)"));
        card.add(Box.createVerticalStrut(6));
        card.add(depositAmount);
        card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Note"));
        card.add(Box.createVerticalStrut(6));
        card.add(depositNote);
        card.add(Box.createVerticalStrut(24));
        card.add(btn);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // ===================== WITHDRAW PANEL =====================
    private JComboBox<String> withdrawAccBox;
    private StyledTextField withdrawAmount;
    private StyledTextField withdrawNote;

    private JPanel buildWithdrawPanel() {
        JPanel panel = makeTransactionPanel("\uD83C\uDF7F Withdraw Funds", Theme.WARNING);

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
            if (!verifyPin()) return;
            try {
                String sel = (String) withdrawAccBox.getSelectedItem();
                if (sel == null) {
                    showError("No account selected.");
                    return;
                }
                String accId = getAccountIdFromLabel(sel);
                double amt = Double.parseDouble(withdrawAmount.getText().trim());
                BankingService.TransactionResult res = bankingService.withdraw(
                        accId, currentUser.getId(), amt, withdrawNote.getText().trim(), "ONLINE");
                if (res != null && res.isSuccess()) {
                    withdrawAmount.setText("");
                    withdrawNote.setText("");
                    refreshTransactionPanels();
                    showReceipt(res.getTransaction(), getAccountNumberFromId(accId));
                } else
                    showError(res != null && res.getErrorMessage() != null ? res.getErrorMessage()
                            : "Insufficient balance or invalid amount.");
            } catch (NumberFormatException ex) {
                showError("Enter a valid amount.");
            }
        });

        card.add(makeLabel("Select Account"));
        card.add(Box.createVerticalStrut(6));
        card.add(withdrawAccBox);
        card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Amount ($)"));
        card.add(Box.createVerticalStrut(6));
        card.add(withdrawAmount);
        card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Note"));
        card.add(Box.createVerticalStrut(6));
        card.add(withdrawNote);
        card.add(Box.createVerticalStrut(24));
        card.add(btn);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // ===================== TRANSFER PANEL =====================
    private JComboBox<String> transferFromBox;
    private JComboBox<String> transferBeneficiaryBox;
    private StyledTextField transferToNumber;
    private StyledTextField transferAmount;
    private StyledTextField transferNote;
    private JCheckBox transferSaveBeneficiary;
    private StyledTextField transferBeneficiaryName;

    private JPanel buildTransferPanel() {
        JPanel panel = makeTransactionPanel("\u2194\uFE0F Transfer Funds", Theme.ACCENT_BLUE);

        CardPanel card = new CardPanel();
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        transferFromBox = new JComboBox<>();
        transferFromBox.setBackground(Theme.BG_INPUT);
        transferFromBox.setForeground(Theme.TEXT_PRIMARY);
        transferFromBox.setFont(Theme.FONT_BODY);

        // Beneficiary quick-pick
        transferBeneficiaryBox = new JComboBox<>();
        transferBeneficiaryBox.setBackground(Theme.BG_INPUT);
        transferBeneficiaryBox.setForeground(Theme.TEXT_PRIMARY);
        transferBeneficiaryBox.setFont(Theme.FONT_BODY);
        transferBeneficiaryBox.addItem("-- Select Beneficiary (optional) --");

        transferBeneficiaryBox.addActionListener(e -> {
            int idx = transferBeneficiaryBox.getSelectedIndex();
            if (idx > 0) {
                List<Beneficiary> bens = bankingService.getBeneficiaries(currentUser.getId());
                if (idx - 1 < bens.size()) {
                    transferToNumber.setText(bens.get(idx - 1).getAccountNumber());
                }
            }
        });

        transferToNumber = new StyledTextField("Recipient Account Number");
        transferAmount = new StyledTextField("Amount to Transfer");
        transferNote = new StyledTextField("Transfer Note (optional)");

        transferSaveBeneficiary = new JCheckBox("Save as Beneficiary");
        transferSaveBeneficiary.setFont(Theme.FONT_SMALL);
        transferSaveBeneficiary.setForeground(Theme.TEXT_SECONDARY);
        transferSaveBeneficiary.setOpaque(false);

        transferBeneficiaryName = new StyledTextField("Beneficiary Name (if saving)");
        transferBeneficiaryName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        GradientButton btn = new GradientButton("Send Transfer");
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addActionListener(e -> {
            if (!verifyPin()) return;
            try {
                String sel = (String) transferFromBox.getSelectedItem();
                if (sel == null) {
                    showError("No source account selected.");
                    return;
                }
                String accId = getAccountIdFromLabel(sel);
                String toNumber = transferToNumber.getText().trim();
                double amt = Double.parseDouble(transferAmount.getText().trim());

                boolean saveBene = transferSaveBeneficiary.isSelected();
                String beneName = saveBene ? transferBeneficiaryName.getText().trim() : null;

                BankingService.TransactionResult res = bankingService.transfer(
                        accId, toNumber, currentUser.getId(), amt,
                        transferNote.getText().trim(), saveBene, beneName, null);
                if (res != null && res.isSuccess()) {
                    transferToNumber.setText("");
                    transferAmount.setText("");
                    transferNote.setText("");
                    transferBeneficiaryName.setText("");
                    transferSaveBeneficiary.setSelected(false);
                    refreshTransactionPanels();
                    showReceipt(res.getTransaction(), getAccountNumberFromId(accId));
                } else
                    showError(
                            res != null && res.getErrorMessage() != null ? res.getErrorMessage() : "Transfer failed.");
            } catch (NumberFormatException ex) {
                showError("Enter a valid amount.");
            }
        });

        card.add(makeLabel("From Account"));
        card.add(Box.createVerticalStrut(6));
        card.add(transferFromBox);
        card.add(Box.createVerticalStrut(12));
        card.add(makeLabel("Quick Pick Beneficiary"));
        card.add(Box.createVerticalStrut(6));
        card.add(transferBeneficiaryBox);
        card.add(Box.createVerticalStrut(12));
        card.add(makeLabel("To Account Number"));
        card.add(Box.createVerticalStrut(6));
        card.add(transferToNumber);
        card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Amount ($)"));
        card.add(Box.createVerticalStrut(6));
        card.add(transferAmount);
        card.add(Box.createVerticalStrut(16));
        card.add(makeLabel("Note"));
        card.add(Box.createVerticalStrut(6));
        card.add(transferNote);
        card.add(Box.createVerticalStrut(12));
        card.add(transferSaveBeneficiary);
        card.add(Box.createVerticalStrut(4));
        card.add(transferBeneficiaryName);
        card.add(Box.createVerticalStrut(24));
        card.add(btn);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void refreshTransactionPanels() {
        List<Account> accounts = bankingService.getUserAccounts(currentUser.getId());
        if (depositAccBox != null)
            depositAccBox.setModel(new DefaultComboBoxModel<>(getAccountLabels(accounts)));
        if (withdrawAccBox != null)
            withdrawAccBox.setModel(new DefaultComboBoxModel<>(getAccountLabels(accounts)));
        if (transferFromBox != null)
            transferFromBox.setModel(new DefaultComboBoxModel<>(getAccountLabels(accounts)));
        if (transferBeneficiaryBox != null) {
            transferBeneficiaryBox.removeAllItems();
            transferBeneficiaryBox.addItem("-- Select Beneficiary (optional) --");
            for (Beneficiary b : bankingService.getBeneficiaries(currentUser.getId())) {
                transferBeneficiaryBox.addItem(b.getDisplayName() + " — " + b.getAccountNumber());
            }
        }
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

    private String getAccountNumberFromId(String accountId) {
        return bankingService.getUserAccounts(currentUser.getId()).stream()
                .filter(a -> a.getId().equals(accountId))
                .map(Account::getAccountNumber)
                .findFirst().orElse("N/A");
    }

    // ===================== HISTORY PANEL =====================
    private JTable historyTable;
    private DefaultTableModel historyModel;

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Transaction History");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        GradientButton exportBtn = new GradientButton("Download Statement", Theme.ACCENT_TEAL, Theme.ACCENT_BLUE);
        exportBtn.addActionListener(e -> exportStatement());

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(exportBtn, BorderLayout.EAST);

        String[] cols = { "Date & Time", "Account No", "Type", "Amount", "Balance After", "Description" };
        historyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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
                if (isSelected)
                    setBackground(Theme.BG_HOVER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        refreshHistoryPanel();
        return panel;
    }

    private void refreshHistoryPanel() {
        if (historyModel == null)
            return;
        historyModel.setRowCount(0);
        List<Transaction> txns = bankingService.getUserTransactions(currentUser.getId());
        List<Account> accounts = bankingService.getUserAccounts(currentUser.getId());

        for (Transaction tx : txns) {
            String accNum = accounts.stream()
                    .filter(a -> a.getId().equals(tx.getAccountId()))
                    .map(Account::getAccountNumber)
                    .findFirst().orElse("N/A");
            historyModel.addRow(new Object[] {
                    tx.getFormattedTimestamp(), accNum,
                    tx.getType().name().replace("_", " "),
                    tx.getFormattedAmount(),
                    String.format("$%,.2f", tx.getBalanceAfter()),
                    tx.getDescription()
            });
        }
    }

    private void exportStatement() {
        if (!verifyPin()) return;

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JComboBox<String> accountBox = new JComboBox<>(getAccountLabels(bankingService.getUserAccounts(currentUser.getId())));
        JTextField fromDate = new JTextField();
        JTextField toDate = new JTextField();

        panel.add(new JLabel("Select Account:"));
        panel.add(accountBox);
        panel.add(new JLabel("From (YYYY-MM-DD):"));
        panel.add(fromDate);
        panel.add(new JLabel("To (YYYY-MM-DD):"));
        panel.add(toDate);

        int result = JOptionPane.showConfirmDialog(this, panel, "Export Statement", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String sel = (String) accountBox.getSelectedItem();
            if (sel == null) return;
            String accId = getAccountIdFromLabel(sel);

            LocalDate from = null;
            LocalDate to = null;
            try {
                if (!fromDate.getText().trim().isEmpty()) {
                    from = LocalDate.parse(fromDate.getText().trim());
                }
                if (!toDate.getText().trim().isEmpty()) {
                    to = LocalDate.parse(toDate.getText().trim());
                }
            } catch (Exception e) {
                showError("Invalid date format. Use YYYY-MM-DD.");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Statement");
            fileChooser.setSelectedFile(new java.io.File("Statement_" + getAccountNumberFromId(accId) + ".pdf"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    StatementService statementService = new StatementService(bankingService);
                    byte[] pdf = statementService.generateStatementPdf(accId, currentUser.getId(), from, to);
                    java.nio.file.Files.write(fileChooser.getSelectedFile().toPath(), pdf);
                    JOptionPane.showMessageDialog(this, "Statement saved successfully.");
                } catch (Exception e) {
                    showError("Error generating statement: " + e.getMessage());
                }
            }
        }
    }

    // ===================== LOANS PANEL =====================
    private JPanel loansPanel;
    private DefaultTableModel loansModel;

    private JPanel buildLoansPanel() {
        loansPanel = new JPanel(new BorderLayout(0, 16));
        loansPanel.setBackground(Theme.BG_DARK);
        loansPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Loans");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        // === Apply for Loan Section ===
        CardPanel applyCard = new CardPanel();
        applyCard.setBackground(Theme.BG_CARD);
        applyCard.setLayout(new BoxLayout(applyCard, BoxLayout.Y_AXIS));
        applyCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel applyTitle = new JLabel("Apply for a Loan");
        applyTitle.setFont(Theme.FONT_HEADING);
        applyTitle.setForeground(Theme.ACCENT_BLUE);
        applyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel applyForm = new JPanel(new GridLayout(2, 4, 10, 8));
        applyForm.setOpaque(false);
        applyForm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JComboBox<Loan.LoanType> loanTypeBox = new JComboBox<>(Loan.LoanType.values());
        loanTypeBox.setBackground(Theme.BG_INPUT);
        loanTypeBox.setForeground(Theme.TEXT_PRIMARY);
        loanTypeBox.setFont(Theme.FONT_BODY);

        JComboBox<String> loanAccBox = new JComboBox<>();
        loanAccBox.setBackground(Theme.BG_INPUT);
        loanAccBox.setForeground(Theme.TEXT_PRIMARY);
        loanAccBox.setFont(Theme.FONT_BODY);

        StyledTextField loanAmountField = new StyledTextField("Loan Amount ($)");
        StyledTextField loanTenureField = new StyledTextField("Tenure (months)");
        StyledTextField loanPurposeField = new StyledTextField("Purpose");

        GradientButton applyBtn = new GradientButton("Apply", Theme.ACCENT_TEAL, Theme.ACCENT_BLUE);
        applyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fill account dropdown
        List<Account> userAccounts = bankingService.getUserAccounts(currentUser.getId());
        for (Account a : userAccounts) {
            if (a.isActive())
                loanAccBox.addItem(a.getAccountNumber() + " [" + a.getId() + "]");
        }

        applyBtn.addActionListener(e -> {
            try {
                String accSel = (String) loanAccBox.getSelectedItem();
                if (accSel == null) { showError("Select a credit account."); return; }
                String accId = accSel.substring(accSel.lastIndexOf('[') + 1, accSel.lastIndexOf(']'));
                double amount = Double.parseDouble(loanAmountField.getText().trim());
                int tenure = Integer.parseInt(loanTenureField.getText().trim());
                Loan.LoanType type = (Loan.LoanType) loanTypeBox.getSelectedItem();
                String purpose = loanPurposeField.getText().trim();

                Loan loan = loanService.applyForLoan(currentUser.getId(), accId, type, amount, tenure, purpose);
                if (loan != null) {
                    JOptionPane.showMessageDialog(this,
                            "Loan application submitted!\nType: " + loan.getTypeDisplay() +
                                    "\nAmount: " + loan.getFormattedPrincipal() +
                                    "\nEMI: " + loan.getFormattedEmi(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loanAmountField.setText("");
                    loanTenureField.setText("");
                    loanPurposeField.setText("");
                    refreshLoansPanel();
                } else {
                    showError("Loan application failed. Check amount/limits.");
                }
            } catch (NumberFormatException ex) {
                showError("Enter valid amount and tenure.");
            }
        });

        applyForm.add(applyTitle);
        applyForm.add(loanTypeBox);
        applyForm.add(loanAccBox);
        applyForm.add(loanAmountField);
        applyForm.add(loanTenureField);
        applyForm.add(loanPurposeField);
        applyForm.add(applyBtn);

        applyCard.add(applyTitle);
        applyCard.add(Box.createVerticalStrut(10));
        applyCard.add(applyForm);

        // === My Loans Table ===
        JLabel myLoansTitle = new JLabel("My Loans");
        myLoansTitle.setFont(Theme.FONT_HEADING);
        myLoansTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = { "ID", "Type", "Principal", "EMI", "Outstanding", "Status", "Progress", "" };
        loansModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        JTable loansTable = new JTable(loansModel);
        loansTable.setBackground(Theme.BG_SURFACE);
        loansTable.setForeground(Theme.TEXT_PRIMARY);
        loansTable.setFont(Theme.FONT_BODY);
        loansTable.setRowHeight(36);
        loansTable.setShowGrid(false);
        loansTable.setSelectionBackground(Theme.BG_HOVER);
        loansTable.getTableHeader().setBackground(Theme.BG_CARD);
        loansTable.getTableHeader().setForeground(Theme.ACCENT_BLUE);
        loansTable.getTableHeader().setFont(Theme.FONT_SUBHEAD);
        loansTable.getColumnModel().getColumn(0).setMinWidth(0);
        loansTable.getColumnModel().getColumn(0).setMaxWidth(0);
        loansTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Repay button column
        loansTable.getColumnModel().getColumn(7).setCellRenderer(new JButtonRenderer("Repay"));
        loansTable.getColumnModel().getColumn(7).setCellEditor(new RepayButtonEditor(loansTable));

        JScrollPane loansScroll = new JScrollPane(loansTable);
        loansScroll.setBackground(Theme.BG_DARK);
        loansScroll.getViewport().setBackground(Theme.BG_SURFACE);
        loansScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);

        JPanel tableSection = new JPanel(new BorderLayout(0, 10));
        tableSection.setOpaque(false);
        tableSection.add(myLoansTitle, BorderLayout.NORTH);
        tableSection.add(loansScroll, BorderLayout.CENTER);

        loansPanel.add(top, BorderLayout.NORTH);
        loansPanel.add(applyCard, BorderLayout.CENTER);
        loansPanel.add(tableSection, BorderLayout.SOUTH);

        refreshLoansPanel();
        return loansPanel;
    }

    private void refreshLoansPanel() {
        if (loansModel == null) return;
        loansModel.setRowCount(0);
        List<Loan> loans = loanService.getUserLoans(currentUser.getId());
        for (Loan loan : loans) {
            loansModel.addRow(new Object[] {
                    loan.getId(),
                    loan.getTypeDisplay(),
                    loan.getFormattedPrincipal(),
                    loan.getFormattedEmi(),
                    loan.getFormattedOutstanding(),
                    loan.getStatus(),
                    String.format("%.0f%%", loan.getProgressPercent()),
                    loan.getStatus() == Loan.LoanStatus.ACTIVE ? "Repay" : ""
            });
        }
    }

    // === Repay Loan Button Renderer/Editor ===

    private class JButtonRenderer extends JButton implements TableCellRenderer {
        JButtonRenderer(String text) { setText(text); setFont(Theme.FONT_SMALL); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean focus, int row, int col) {
            setEnabled(val != null && !val.toString().isEmpty());
            return this;
        }
    }

    private class RepayButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn;
        private String loanId;

        RepayButtonEditor(JTable table) {
            btn = new JButton("Repay");
            btn.setFont(Theme.FONT_SMALL);
            btn.setBackground(Theme.ACCENT_TEAL);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                if (loanId == null) return;
                if (!verifyPin()) { fireEditingStopped(); return; }

                Loan loan = loanService.getLoanById(loanId);
                if (loan == null) { fireEditingStopped(); return; }

                // Pick repayment amount
                JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
                panel.setBackground(Theme.BG_CARD);

                JComboBox<String> accBox = new JComboBox<>();
                for (Account a : bankingService.getUserAccounts(currentUser.getId())) {
                    if (a.isActive() && a.getType() != Account.AccountType.FIXED_DEPOSIT)
                        accBox.addItem(a.getAccountNumber() + " (" + a.getFormattedBalance() + ") [" + a.getId() + "]");
                }

                StyledTextField amtField = new StyledTextField("Repayment Amount ($)");
                amtField.setText(String.format("%.2f", loan.getEmiAmount()));

                panel.add(new JLabel("From Account:") {{ setForeground(Theme.TEXT_PRIMARY); }});
                panel.add(accBox);
                panel.add(new JLabel("Amount:") {{ setForeground(Theme.TEXT_PRIMARY); }});
                panel.add(amtField);

                int res = JOptionPane.showConfirmDialog(CustomerDashboard.this, panel,
                        "Repay Loan — " + loan.getTypeDisplay(),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (res == JOptionPane.OK_OPTION) {
                    try {
                        String accSel = (String) accBox.getSelectedItem();
                        String accId = accSel.substring(accSel.lastIndexOf('[') + 1, accSel.lastIndexOf(']'));
                        double amt = Double.parseDouble(amtField.getText().trim());
                        BankingService.TransactionResult result = loanService.makeRepayment(
                                loanId, accId, currentUser.getId(), amt);
                        if (result.isSuccess()) {
                            refreshLoansPanel();
                            showReceipt(result.getTransaction(),
                                    getAccountNumberFromId(accId));
                        } else {
                            showError(result.getErrorMessage());
                        }
                    } catch (Exception ex) {
                        showError("Invalid input.");
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            loanId = (val != null && !val.toString().isEmpty()) ? (String) t.getValueAt(row, 0) : null;
            btn.setText(val != null && !val.toString().isEmpty() ? "Repay" : "");
            btn.setEnabled(val != null && !val.toString().isEmpty());
            return btn;
        }

        @Override
        public Object getCellEditorValue() { return btn.getText(); }
    }

    // ===================== BENEFICIARIES PANEL =====================
    private JPanel beneficiariesPanel;
    private DefaultTableModel beneficiariesModel;

    private JPanel buildBeneficiariesPanel() {
        beneficiariesPanel = new JPanel(new BorderLayout(0, 16));
        beneficiariesPanel.setBackground(Theme.BG_DARK);
        beneficiariesPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Manage Beneficiaries");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        // Add beneficiary form
        CardPanel addCard = new CardPanel();
        addCard.setBackground(Theme.BG_CARD);
        addCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        StyledTextField nameField = new StyledTextField("Full Name");
        nameField.setPreferredSize(new Dimension(160, 36));
        StyledTextField accNumField = new StyledTextField("Account Number");
        accNumField.setPreferredSize(new Dimension(180, 36));
        StyledTextField nickField = new StyledTextField("Nickname (optional)");
        nickField.setPreferredSize(new Dimension(140, 36));

        GradientButton addBtn = new GradientButton("+ Add Beneficiary", Theme.ACCENT_TEAL, Theme.ACCENT_BLUE);
        addBtn.setPreferredSize(new Dimension(170, 36));
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String accNum = accNumField.getText().trim();
            String nick = nickField.getText().trim();
            if (name.isEmpty() || accNum.isEmpty()) {
                showError("Name and account number are required.");
                return;
            }
            // Check account exists
            var target = bankingService.getAllAccounts().stream()
                    .filter(a -> a.getAccountNumber().equals(accNum) && a.isActive())
                    .findFirst();
            if (target.isEmpty()) {
                showError("Account number not found or inactive.");
                return;
            }
            boolean exists = bankingService.getBeneficiaries(currentUser.getId()).stream()
                    .anyMatch(b -> b.getAccountNumber().equals(accNum));
            if (exists) {
                showError("Beneficiary with this account number already exists.");
                return;
            }
            Beneficiary ben = new Beneficiary(currentUser.getId(), name, accNum, nick);
            bankingService.addBeneficiary(ben);
            JOptionPane.showMessageDialog(this, "Beneficiary added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText("");
            accNumField.setText("");
            nickField.setText("");
            refreshBeneficiariesPanel();
        });

        addCard.add(new JLabel("Add:") {{
            setForeground(Theme.TEXT_SECONDARY);
            setFont(Theme.FONT_SUBHEAD);
        }});
        addCard.add(nameField);
        addCard.add(accNumField);
        addCard.add(nickField);
        addCard.add(addBtn);

        // Table
        String[] cols = { "Name", "Nickname", "Account Number", "Added", "Actions" };
        beneficiariesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        JTable table = new JTable(beneficiariesModel);
        table.setBackground(Theme.BG_SURFACE);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.getTableHeader().setBackground(Theme.BG_CARD);
        table.getTableHeader().setForeground(Theme.ACCENT_BLUE);
        table.getTableHeader().setFont(Theme.FONT_SUBHEAD);
        table.getColumnModel().getColumn(4).setCellRenderer(new JButtonRenderer("Remove"));
        table.getColumnModel().getColumn(4).setCellEditor(new RemoveBeneficiaryEditor(table));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);

        beneficiariesPanel.add(top, BorderLayout.NORTH);
        beneficiariesPanel.add(addCard, BorderLayout.CENTER);
        beneficiariesPanel.add(scroll, BorderLayout.SOUTH);

        refreshBeneficiariesPanel();
        return beneficiariesPanel;
    }

    private void refreshBeneficiariesPanel() {
        if (beneficiariesModel == null) return;
        beneficiariesModel.setRowCount(0);
        for (Beneficiary b : bankingService.getBeneficiaries(currentUser.getId())) {
            beneficiariesModel.addRow(new Object[] {
                    b.getName(), b.getNickname(), b.getAccountNumber(),
                    b.getAddedAt().toLocalDate(), "Remove"
            });
        }
    }

    private class RemoveBeneficiaryEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn;
        private int editRow;

        RemoveBeneficiaryEditor(JTable table) {
            btn = new JButton("Remove");
            btn.setFont(Theme.FONT_SMALL);
            btn.setBackground(Theme.DANGER);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                List<Beneficiary> bens = bankingService.getBeneficiaries(currentUser.getId());
                if (editRow >= 0 && editRow < bens.size()) {
                    int conf = JOptionPane.showConfirmDialog(CustomerDashboard.this,
                            "Remove " + bens.get(editRow).getName() + "?",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                    if (conf == JOptionPane.YES_OPTION) {
                        bankingService.removeBeneficiary(bens.get(editRow).getId());
                        refreshBeneficiariesPanel();
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            editRow = row;
            return btn;
        }

        @Override
        public Object getCellEditorValue() { return "Remove"; }
    }

    // ===================== SCHEDULED TRANSFERS PANEL =====================
    private JPanel scheduledPanel;
    private DefaultTableModel scheduledModel;

    private JPanel buildScheduledPanel() {
        scheduledPanel = new JPanel(new BorderLayout(0, 16));
        scheduledPanel.setBackground(Theme.BG_DARK);
        scheduledPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Scheduled Transfers");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        // Create form
        CardPanel createCard = new CardPanel();
        createCard.setBackground(Theme.BG_CARD);
        createCard.setLayout(new BoxLayout(createCard, BoxLayout.Y_AXIS));
        createCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel formTitle = new JLabel("Schedule a Transfer");
        formTitle.setFont(Theme.FONT_HEADING);
        formTitle.setForeground(Theme.ACCENT_BLUE);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel form = new JPanel(new GridLayout(2, 4, 10, 8));
        form.setOpaque(false);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JComboBox<String> schedFromBox = new JComboBox<>();
        schedFromBox.setBackground(Theme.BG_INPUT);
        schedFromBox.setForeground(Theme.TEXT_PRIMARY);
        schedFromBox.setFont(Theme.FONT_BODY);

        StyledTextField schedToField = new StyledTextField("Recipient Account No.");
        StyledTextField schedAmountField = new StyledTextField("Amount ($)");
        StyledTextField schedDateField = new StyledTextField("Date (YYYY-MM-DD)");
        schedDateField.setText(LocalDate.now().toString());
        StyledTextField schedDescField = new StyledTextField("Description");

        JCheckBox recurringBox = new JCheckBox("Monthly Recurring");
        recurringBox.setFont(Theme.FONT_SMALL);
        recurringBox.setForeground(Theme.TEXT_SECONDARY);
        recurringBox.setOpaque(false);

        GradientButton createBtn = new GradientButton("Schedule", Theme.ACCENT_TEAL, Theme.ACCENT_BLUE);

        // Fill account dropdown
        for (Account a : bankingService.getUserAccounts(currentUser.getId())) {
            if (a.isActive() && a.getType() != Account.AccountType.FIXED_DEPOSIT)
                schedFromBox.addItem(a.getAccountNumber() + " [" + a.getId() + "]");
        }

        createBtn.addActionListener(e -> {
            try {
                String fromSel = (String) schedFromBox.getSelectedItem();
                if (fromSel == null) { showError("Select source account."); return; }
                String accId = fromSel.substring(fromSel.lastIndexOf('[') + 1, fromSel.lastIndexOf(']'));
                String toAccNum = schedToField.getText().trim();
                double amount = Double.parseDouble(schedAmountField.getText().trim());
                LocalDate date = LocalDate.parse(schedDateField.getText().trim());
                String desc = schedDescField.getText().trim();
                boolean recurring = recurringBox.isSelected();

                BankingService.TransactionResult res = extService.scheduleTransfer(
                        currentUser.getId(), accId, toAccNum, amount, desc, date, recurring);
                if (res.isSuccess()) {
                    JOptionPane.showMessageDialog(this,
                            "Transfer scheduled for " + date + "!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    schedToField.setText("");
                    schedAmountField.setText("");
                    schedDescField.setText("");
                    refreshScheduledPanel();
                } else {
                    showError(res.getErrorMessage());
                }
            } catch (NumberFormatException ex) {
                showError("Enter a valid amount.");
            } catch (java.time.format.DateTimeParseException ex) {
                showError("Invalid date format. Use YYYY-MM-DD.");
            }
        });

        form.add(schedFromBox);
        form.add(schedToField);
        form.add(schedAmountField);
        form.add(schedDateField);
        form.add(schedDescField);
        form.add(recurringBox);
        form.add(createBtn);

        createCard.add(formTitle);
        createCard.add(Box.createVerticalStrut(10));
        createCard.add(form);

        // Table
        JLabel listTitle = new JLabel("My Scheduled Transfers");
        listTitle.setFont(Theme.FONT_HEADING);
        listTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = { "ID", "To Account", "Amount", "Scheduled Date", "Recurring", "Status", "" };
        scheduledModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        JTable schedTable = new JTable(scheduledModel);
        schedTable.setBackground(Theme.BG_SURFACE);
        schedTable.setForeground(Theme.TEXT_PRIMARY);
        schedTable.setFont(Theme.FONT_BODY);
        schedTable.setRowHeight(36);
        schedTable.setShowGrid(false);
        schedTable.getTableHeader().setBackground(Theme.BG_CARD);
        schedTable.getTableHeader().setForeground(Theme.ACCENT_BLUE);
        schedTable.getTableHeader().setFont(Theme.FONT_SUBHEAD);
        schedTable.getColumnModel().getColumn(0).setMinWidth(0);
        schedTable.getColumnModel().getColumn(0).setMaxWidth(0);
        schedTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        schedTable.getColumnModel().getColumn(6).setCellRenderer(new JButtonRenderer("Cancel"));
        schedTable.getColumnModel().getColumn(6).setCellEditor(new CancelScheduledEditor(schedTable));

        JScrollPane scroll = new JScrollPane(schedTable);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);

        JPanel tableSection = new JPanel(new BorderLayout(0, 10));
        tableSection.setOpaque(false);
        tableSection.add(listTitle, BorderLayout.NORTH);
        tableSection.add(scroll, BorderLayout.CENTER);

        scheduledPanel.add(top, BorderLayout.NORTH);
        scheduledPanel.add(createCard, BorderLayout.CENTER);
        scheduledPanel.add(tableSection, BorderLayout.SOUTH);

        refreshScheduledPanel();
        return scheduledPanel;
    }

    private void refreshScheduledPanel() {
        if (scheduledModel == null) return;
        scheduledModel.setRowCount(0);
        for (ScheduledTransfer st : extService.getScheduledTransfers(currentUser.getId())) {
            String status = st.isExecuted() ?
                    (st.isRecurringMonthly() ? "Cancelled" : "Completed") :
                    (st.isRecurringMonthly() ? "Recurring" : "Pending");
            boolean canCancel = !st.isExecuted();
            scheduledModel.addRow(new Object[] {
                    st.getId(),
                    st.getToAccountNumber(),
                    String.format("$%,.2f", st.getAmount()),
                    st.getScheduledDate().toString(),
                    st.isRecurringMonthly() ? "Yes" : "No",
                    status,
                    canCancel ? "Cancel" : ""
            });
        }
    }

    private class CancelScheduledEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn;
        private String transferId;

        CancelScheduledEditor(JTable table) {
            btn = new JButton("Cancel");
            btn.setFont(Theme.FONT_SMALL);
            btn.setBackground(Theme.DANGER);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                if (transferId != null) {
                    int conf = JOptionPane.showConfirmDialog(CustomerDashboard.this,
                            "Cancel this scheduled transfer?",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                    if (conf == JOptionPane.YES_OPTION) {
                        extService.cancelScheduledTransfer(transferId);
                        refreshScheduledPanel();
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            transferId = (val != null && !val.toString().isEmpty()) ? (String) t.getValueAt(row, 0) : null;
            btn.setText(val != null && !val.toString().isEmpty() ? "Cancel" : "");
            btn.setEnabled(val != null && !val.toString().isEmpty());
            return btn;
        }

        @Override
        public Object getCellEditorValue() { return btn.getText(); }
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
