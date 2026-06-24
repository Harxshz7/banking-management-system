package banking.data;

import banking.models.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    private static final String DATA_DIR = "banking_data";
    private static final String USERS_FILE         = DATA_DIR + File.separator + "users.dat";
    private static final String ACCOUNTS_FILE      = DATA_DIR + File.separator + "accounts.dat";
    private static final String TRANSACTIONS_FILE  = DATA_DIR + File.separator + "transactions.dat";
    private static final String LOANS_FILE         = DATA_DIR + File.separator + "loans.dat";
    private static final String BENEFICIARIES_FILE = DATA_DIR + File.separator + "beneficiaries.dat";

    private List<User> users = new ArrayList<>();
    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();
    private List<Beneficiary> beneficiaries = new ArrayList<>();

    private static DataManager instance;

    private DataManager() {
        initDataDirectory();
        loadAll();
        seedDefaultData();
    }

    public static DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    public static void resetInstance() { instance = null; }

    private void initDataDirectory() {
        new File(DATA_DIR).mkdirs();
    }

    private void seedDefaultData() {
        if (!users.isEmpty()) return;

        User admin = new User("admin", "admin123", "System Administrator",
                "admin@bankpro.com", "1800000000", "ADMIN");
        admin.setTransactionPin("0000");
        users.add(admin);

        User john = new User("john", "john123", "John Doe",
                "john@email.com", "9876543210", "CUSTOMER");
        john.setAddress("123 Main Street, NY");
        john.setTransactionPin("1234");
        users.add(john);

        User jane = new User("jane", "jane123", "Jane Smith",
                "jane@email.com", "9123456789", "CUSTOMER");
        jane.setAddress("456 Oak Avenue, LA");
        jane.setTransactionPin("5678");
        users.add(jane);

        // John's accounts
        Account johnSavings  = new Account(john.getId(), Account.AccountType.SAVINGS, 8500.00);
        Account johnChecking = new Account(john.getId(), Account.AccountType.CHECKING, 3200.00);
        Account johnFD       = new Account(john.getId(), Account.AccountType.FIXED_DEPOSIT, 25000.00);
        accounts.addAll(List.of(johnSavings, johnChecking, johnFD));

        // Jane's accounts
        Account janeSavings  = new Account(jane.getId(), Account.AccountType.SAVINGS, 12000.00);
        Account janeChecking = new Account(jane.getId(), Account.AccountType.CHECKING, 4500.00);
        accounts.addAll(List.of(janeSavings, janeChecking));

        // Opening transactions
        List.of(
            new Transaction(johnSavings.getId(),  john.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 8500,  8500,  "Account Opened", null),
            new Transaction(johnChecking.getId(), john.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 3200,  3200,  "Account Opened", null),
            new Transaction(johnFD.getId(),       john.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 25000, 25000, "Fixed Deposit Opened", null),
            new Transaction(janeSavings.getId(),  jane.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 12000, 12000, "Account Opened", null),
            new Transaction(janeChecking.getId(), jane.getId(), Transaction.TransactionType.ACCOUNT_OPENED, 4500,  4500,  "Account Opened", null)
        ).forEach(transactions::add);

        // Sample beneficiary for John
        beneficiaries.add(new Beneficiary(john.getId(), "Jane Smith",
                janeSavings.getAccountNumber(), "Jane"));

        saveAll();
    }

    // ===================== USER OPERATIONS =====================
    public List<User> getAllUsers() { return new ArrayList<>(users); }
    public Optional<User> findUserById(String id) { return users.stream().filter(u -> u.getId().equals(id)).findFirst(); }
    public Optional<User> findUserByUsername(String username) { return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst(); }
    public boolean addUser(User user) {
        if (findUserByUsername(user.getUsername()).isPresent()) return false;
        users.add(user); saveAll(); return true;
    }
    public void updateUser(User user) { saveAll(); }
    public void deleteUser(String userId) {
        users.removeIf(u -> u.getId().equals(userId));
        accounts.removeIf(a -> a.getUserId().equals(userId));
        transactions.removeIf(t -> t.getUserId().equals(userId));
        loans.removeIf(l -> l.getUserId().equals(userId));
        beneficiaries.removeIf(b -> b.getUserId().equals(userId));
        saveAll();
    }

    // ===================== ACCOUNT OPERATIONS =====================
    public List<Account> getAllAccounts() { return new ArrayList<>(accounts); }
    public List<Account> getAccountsByUser(String userId) {
        return accounts.stream().filter(a -> a.getUserId().equals(userId)).collect(Collectors.toList());
    }
    public Optional<Account> findAccountById(String id) { return accounts.stream().filter(a -> a.getId().equals(id)).findFirst(); }
    public Optional<Account> findAccountByNumber(String number) { return accounts.stream().filter(a -> a.getAccountNumber().equals(number)).findFirst(); }
    public void addAccount(Account account) { accounts.add(account); saveAll(); }
    public void updateAccount(Account account) { saveAll(); }
    public void closeAccount(String accountId) {
        accounts.stream().filter(a -> a.getId().equals(accountId)).findFirst().ifPresent(a -> a.setActive(false));
        saveAll();
    }

    // ===================== TRANSACTION OPERATIONS =====================
    public List<Transaction> getAllTransactions() { return new ArrayList<>(transactions); }
    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactions.stream().filter(t -> t.getAccountId().equals(accountId))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    public List<Transaction> getTransactionsByUser(String userId) {
        return transactions.stream().filter(t -> t.getUserId().equals(userId))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    public void addTransaction(Transaction tx) { transactions.add(tx); saveAll(); }

    // ===================== LOAN OPERATIONS =====================
    public List<Loan> getAllLoans() { return new ArrayList<>(loans); }
    public List<Loan> getLoansByUser(String userId) {
        return loans.stream().filter(l -> l.getUserId().equals(userId)).collect(Collectors.toList());
    }
    public Optional<Loan> findLoanById(String id) { return loans.stream().filter(l -> l.getId().equals(id)).findFirst(); }
    public void addLoan(Loan loan) { loans.add(loan); saveAll(); }
    public void updateLoan(Loan loan) { saveAll(); }

    // ===================== BENEFICIARY OPERATIONS =====================
    public List<Beneficiary> getAllBeneficiaries() { return new ArrayList<>(beneficiaries); }
    public List<Beneficiary> getBeneficiariesByUser(String userId) {
        return beneficiaries.stream().filter(b -> b.getUserId().equals(userId)).collect(Collectors.toList());
    }
    public void addBeneficiary(Beneficiary b) { beneficiaries.add(b); saveAll(); }
    public void removeBeneficiary(String id) { beneficiaries.removeIf(b -> b.getId().equals(id)); saveAll(); }

    // ===================== PERSISTENCE =====================
    private void loadAll() {
        users         = loadFile(USERS_FILE);
        accounts      = loadFile(ACCOUNTS_FILE);
        transactions  = loadFile(TRANSACTIONS_FILE);
        loans         = loadFile(LOANS_FILE);
        beneficiaries = loadFile(BENEFICIARIES_FILE);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> loadFile(String path) {
        File file = new File(path);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) { return new ArrayList<>(); }
    }

    public void saveAll() {
        saveFile(USERS_FILE, users);
        saveFile(ACCOUNTS_FILE, accounts);
        saveFile(TRANSACTIONS_FILE, transactions);
        saveFile(LOANS_FILE, loans);
        saveFile(BENEFICIARIES_FILE, beneficiaries);
    }

    private <T> void saveFile(String path, List<T> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(data);
        } catch (Exception e) { System.err.println("Save error: " + e.getMessage()); }
    }
}
