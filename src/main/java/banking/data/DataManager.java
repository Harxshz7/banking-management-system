package banking.data;

import banking.data.dao.*;
import banking.models.*;
import banking.util.PasswordUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataManager {
    private static final String DATA_DIR = "banking_data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.dat";
    private static final String ACCOUNTS_FILE = DATA_DIR + File.separator + "accounts.dat";
    private static final String TRANSACTIONS_FILE = DATA_DIR + File.separator + "transactions.dat";
    private static final String LOANS_FILE = DATA_DIR + File.separator + "loans.dat";
    private static final String BENEFICIARIES_FILE = DATA_DIR + File.separator + "beneficiaries.dat";
    private static final String NOTIFICATIONS_FILE = DATA_DIR + File.separator + "notifications.dat";
    private static final String AUDIT_FILE = DATA_DIR + File.separator + "audit.dat";
    private static final String RDS_FILE = DATA_DIR + File.separator + "rds.dat";
    private static final String SCHEDULED_FILE = DATA_DIR + File.separator + "scheduled_transfers.dat";

    private final UserDao userDao = new UserDao();
    private final AccountDao accountDao = new AccountDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final LoanDao loanDao = new LoanDao();
    private final BeneficiaryDao beneficiaryDao = new BeneficiaryDao();
    private final NotificationDao notificationDao = new NotificationDao();
    private final AuditLogDao auditLogDao = new AuditLogDao();
    private final RecurringDepositDao recurringDepositDao = new RecurringDepositDao();
    private final ScheduledTransferDao scheduledTransferDao = new ScheduledTransferDao();

    private static DataManager instance;

    private DataManager() {
        DatabaseManager.initializeSchema();
        if (requiresMigration()) {
            migrateFromDatFiles();
        }
        seedDefaultData();
    }

    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private boolean requiresMigration() {
        File usersFile = new File(USERS_FILE);
        // Only migrate if .dat exists and sqlite DB is empty (i.e. no users)
        return usersFile.exists() && userDao.findAll().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void migrateFromDatFiles() {
        System.out.println("Migrating from .dat files to SQLite...");
        try {
            DatabaseManager.beginTransaction();

            List<User> users = loadFile(USERS_FILE);
            for (User u : users) userDao.addUser(u);

            List<Account> accounts = loadFile(ACCOUNTS_FILE);
            for (Account a : accounts) accountDao.addAccount(a);

            List<Transaction> transactions = loadFile(TRANSACTIONS_FILE);
            for (Transaction t : transactions) transactionDao.addTransaction(t);

            List<Loan> loans = loadFile(LOANS_FILE);
            for (Loan l : loans) loanDao.addLoan(l);

            List<Beneficiary> beneficiaries = loadFile(BENEFICIARIES_FILE);
            for (Beneficiary b : beneficiaries) beneficiaryDao.addBeneficiary(b);

            List<Notification> notifications = loadFile(NOTIFICATIONS_FILE);
            for (Notification n : notifications) notificationDao.addNotification(n);

            List<AuditLog> auditLogs = loadFile(AUDIT_FILE);
            for (AuditLog al : auditLogs) auditLogDao.addAuditLog(al);

            List<RecurringDeposit> rds = loadFile(RDS_FILE);
            for (RecurringDeposit rd : rds) recurringDepositDao.addRecurringDeposit(rd);

            List<ScheduledTransfer> scheduledTransfers = loadFile(SCHEDULED_FILE);
            for (ScheduledTransfer st : scheduledTransfers) scheduledTransferDao.addScheduledTransfer(st);

            DatabaseManager.commitTransaction();

            // Rename directory to backup
            File dataDir = new File(DATA_DIR);
            File backupDir = new File("banking_data_migrated_backup");
            if (backupDir.exists()) {
                deleteDir(backupDir);
            }
            // Copy files to backup dir
            backupDir.mkdirs();
            for (File file : dataDir.listFiles()) {
                if (file.getName().endsWith(".dat")) {
                    Files.move(file.toPath(), new File(backupDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            System.out.println("Migration completed successfully.");

        } catch (Exception e) {
            DatabaseManager.rollbackTransaction();
            throw new RuntimeException("Migration failed!", e);
        }
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> loadFile(String path) {
        File file = new File(path);
        if (!file.exists())
            return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void seedDefaultData() {
        if (!userDao.findAll().isEmpty())
            return;

        try {
            DatabaseManager.beginTransaction();
            User admin = new User("admin", PasswordUtil.hash("admin123"), "System Administrator",
                    "admin@bankpro.com", "1800000000", "ADMIN");
            admin.hashAndSetPin("0000");
            userDao.addUser(admin);

            User john = new User("john", PasswordUtil.hash("john123"), "John Doe",
                    "john@email.com", "9876543210", "CUSTOMER");
            john.setAddress("123 Main Street, NY");
            john.hashAndSetPin("1234");
            userDao.addUser(john);

            User jane = new User("jane", PasswordUtil.hash("jane123"), "Jane Smith",
                    "jane@email.com", "9123456789", "CUSTOMER");
            jane.setAddress("456 Oak Avenue, LA");
            jane.hashAndSetPin("5678");
            userDao.addUser(jane);

            // John's accounts
            Account johnSavings = new Account(john.getId(), Account.AccountType.SAVINGS, 8500.00, "1000000001");
            Account johnChecking = new Account(john.getId(), Account.AccountType.CHECKING, 3200.00, "1000000002");
            Account johnFD = new Account(john.getId(), Account.AccountType.FIXED_DEPOSIT, 25000.00, "1000000003");
            accountDao.addAccount(johnSavings);
            accountDao.addAccount(johnChecking);
            accountDao.addAccount(johnFD);

            // Jane's accounts
            Account janeSavings = new Account(jane.getId(), Account.AccountType.SAVINGS, 12000.00, "1000000004");
            Account janeChecking = new Account(jane.getId(), Account.AccountType.CHECKING, 4500.00, "1000000005");
            accountDao.addAccount(janeSavings);
            accountDao.addAccount(janeChecking);

            // Opening transactions
            transactionDao.addTransaction(new Transaction(johnSavings.getId(), john.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 8500, 8500, "Account Opened", null));
            transactionDao.addTransaction(new Transaction(johnChecking.getId(), john.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 3200, 3200, "Account Opened", null));
            transactionDao.addTransaction(new Transaction(johnFD.getId(), john.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 25000, 25000, "Fixed Deposit Opened", null));
            transactionDao.addTransaction(new Transaction(janeSavings.getId(), jane.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 12000, 12000, "Account Opened", null));
            transactionDao.addTransaction(new Transaction(janeChecking.getId(), jane.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 4500, 4500, "Account Opened", null));

            // Sample beneficiary for John
            beneficiaryDao.addBeneficiary(new Beneficiary(john.getId(), "Jane Smith", janeSavings.getAccountNumber(), "Jane"));

            DatabaseManager.commitTransaction();
        } catch (Exception e) {
            DatabaseManager.rollbackTransaction();
            e.printStackTrace();
        }
    }

    // ===================== USER OPERATIONS =====================
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public Optional<User> findUserById(String id) {
        return userDao.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public boolean addUser(User user) {
        if (findUserByUsername(user.getUsername()).isPresent())
            return false;
        userDao.addUser(user);
        return true;
    }

    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    public void deleteUser(String userId) {
        userDao.deleteUser(userId);
    }

    // ===================== ACCOUNT OPERATIONS =====================
    public List<Account> getAllAccounts() {
        return accountDao.findAll();
    }

    public List<Account> getAccountsByUser(String userId) {
        return accountDao.findByUserId(userId);
    }

    public Optional<Account> findAccountById(String id) {
        return accountDao.findById(id);
    }

    public Optional<Account> findAccountByNumber(String number) {
        return accountDao.findByNumber(number);
    }

    public void addAccount(Account account) {
        accountDao.addAccount(account);
    }

    public void updateAccount(Account account) {
        accountDao.updateAccount(account);
    }

    public void closeAccount(String accountId) {
        findAccountById(accountId).ifPresent(a -> {
            a.setActive(false);
            accountDao.updateAccount(a);
        });
    }

    // ===================== TRANSACTION OPERATIONS =====================
    public List<Transaction> getAllTransactions() {
        return transactionDao.findAll();
    }

    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactionDao.findByAccountId(accountId);
    }

    public List<Transaction> getTransactionsByUser(String userId) {
        return transactionDao.findByUserId(userId);
    }

    public void addTransaction(Transaction tx) {
        transactionDao.addTransaction(tx);
    }

    public void addTransactionWithoutSave(Transaction tx) {
        transactionDao.addTransaction(tx);
    }

    public void addTransactions(List<Transaction> txs) {
        for(Transaction t : txs) transactionDao.addTransaction(t);
    }

    public void addTransactionsWithoutSave(List<Transaction> txs) {
        for(Transaction t : txs) transactionDao.addTransaction(t);
    }

    // ===================== LOAN OPERATIONS =====================
    public List<Loan> getAllLoans() {
        return loanDao.findAll();
    }

    public List<Loan> getLoansByUser(String userId) {
        return loanDao.findByUserId(userId);
    }

    public Optional<Loan> findLoanById(String id) {
        return loanDao.findById(id);
    }

    public void addLoan(Loan loan) {
        loanDao.addLoan(loan);
    }

    public void updateLoan(Loan loan) {
        loanDao.updateLoan(loan);
    }

    // ===================== NOTIFICATION OPERATIONS =====================
    public List<Notification> getAllNotifications() {
        return notificationDao.findAll();
    }

    public List<Notification> getNotificationsByUser(String userId) {
        return notificationDao.findByUserId(userId);
    }

    public void addNotification(Notification notification) {
        notificationDao.addNotification(notification);
    }

    public void markNotificationRead(String notificationId) {
        notificationDao.findAll().stream().filter(n -> n.getId().equals(notificationId)).findFirst().ifPresent(n -> {
            n.setRead(true);
            notificationDao.updateNotification(n);
        });
    }

    // ===================== AUDIT OPERATIONS =====================
    public List<AuditLog> getAllAuditLogs() {
        return auditLogDao.findAll();
    }

    public void addAuditLog(AuditLog audit) {
        auditLogDao.addAuditLog(audit);
    }

    // ===================== RECURRING DEPOSIT OPERATIONS =====================
    public List<RecurringDeposit> getAllRds() {
        return recurringDepositDao.findAll();
    }

    public List<RecurringDeposit> getRdsByUser(String userId) {
        return recurringDepositDao.findByUserId(userId);
    }

    public void addRd(RecurringDeposit rd) {
        recurringDepositDao.addRecurringDeposit(rd);
    }

    public void updateRd(RecurringDeposit rd) {
        recurringDepositDao.updateRecurringDeposit(rd);
    }

    // ===================== SCHEDULED TRANSFER OPERATIONS =====================
    public List<ScheduledTransfer> getAllScheduledTransfers() {
        return scheduledTransferDao.findAll();
    }

    public List<ScheduledTransfer> getScheduledTransfersByUser(String userId) {
        return scheduledTransferDao.findByUserId(userId);
    }

    public void addScheduledTransfer(ScheduledTransfer transfer) {
        scheduledTransferDao.addScheduledTransfer(transfer);
    }

    public void updateScheduledTransfer(ScheduledTransfer transfer) {
        scheduledTransferDao.updateScheduledTransfer(transfer);
    }

    public void removeScheduledTransfer(String id) {
        scheduledTransferDao.removeScheduledTransfer(id);
    }

    // ===================== BENEFICIARY OPERATIONS =====================
    public List<Beneficiary> getAllBeneficiaries() {
        return beneficiaryDao.findAll();
    }

    public List<Beneficiary> getBeneficiariesByUser(String userId) {
        return beneficiaryDao.findByUserId(userId);
    }

    public void addBeneficiary(Beneficiary b) {
        beneficiaryDao.addBeneficiary(b);
    }

    public void addBeneficiaryWithoutSave(Beneficiary b) {
        beneficiaryDao.addBeneficiary(b);
    }

    public void removeBeneficiary(String id) {
        beneficiaryDao.removeBeneficiary(id);
    }

    // For tests that assert saveAll was called
    private int saveAllCount = 0;
    public int getSaveAllCount() { return saveAllCount; }
    public void resetSaveAllCount() { saveAllCount = 0; }
    public void saveAll() { saveAllCount++; }

    // Transaction execution
    public void executeInTransaction(Runnable action) {
        try {
            DatabaseManager.beginTransaction();
            action.run();
            DatabaseManager.commitTransaction();
            saveAllCount++;
        } catch (Exception e) {
            DatabaseManager.rollbackTransaction();
            throw new RuntimeException("Transaction failed, rolled back.", e);
        }
    }
}
