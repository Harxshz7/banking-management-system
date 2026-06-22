package banking.data;

import banking.models.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    private static final String DATA_DIR = "banking_data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.dat";
    private static final String ACCOUNTS_FILE = DATA_DIR + File.separator + "accounts.dat";
    private static final String TRANSACTIONS_FILE = DATA_DIR + File.separator + "transactions.dat";

    private List<User> users = new ArrayList<>();
    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    private static DataManager instance;

    private DataManager() {
        initDataDirectory();
        loadAll();
        seedDefaultData();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void initDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void seedDefaultData() {
        if (users.isEmpty()) {
            // Create default admin
            User admin = new User("admin", "admin123", "System Administrator",
                    "admin@bank.com", "1234567890", "ADMIN");
            users.add(admin);

            // Create a sample customer
            User customer = new User("john", "john123", "John Doe",
                    "john@email.com", "9876543210", "CUSTOMER");
            users.add(customer);

            // Create sample accounts
            Account savings = new Account(customer.getId(), Account.AccountType.SAVINGS, 5000.00);
            Account checking = new Account(customer.getId(), Account.AccountType.CHECKING, 1200.00);
            accounts.add(savings);
            accounts.add(checking);

            // Add opening transactions
            transactions.add(new Transaction(savings.getId(), customer.getId(),
                    Transaction.TransactionType.ACCOUNT_OPENED, 5000.00, 5000.00,
                    "Account Opened", null));
            transactions.add(new Transaction(checking.getId(), customer.getId(),
                    Transaction.TransactionType.ACCOUNT_OPENED, 1200.00, 1200.00,
                    "Account Opened", null));

            saveAll();
        }
    }

    // ===================== USER OPERATIONS =====================

    public List<User> getAllUsers() { return new ArrayList<>(users); }

    public Optional<User> findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public Optional<User> findUserByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public boolean addUser(User user) {
        if (findUserByUsername(user.getUsername()).isPresent()) return false;
        users.add(user);
        saveAll();
        return true;
    }

    public void updateUser(User user) {
        saveAll();
    }

    public void deleteUser(String userId) {
        users.removeIf(u -> u.getId().equals(userId));
        accounts.removeIf(a -> a.getUserId().equals(userId));
        transactions.removeIf(t -> t.getUserId().equals(userId));
        saveAll();
    }

    // ===================== ACCOUNT OPERATIONS =====================

    public List<Account> getAllAccounts() { return new ArrayList<>(accounts); }

    public List<Account> getAccountsByUser(String userId) {
        return accounts.stream().filter(a -> a.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public Optional<Account> findAccountById(String id) {
        return accounts.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public Optional<Account> findAccountByNumber(String number) {
        return accounts.stream().filter(a -> a.getAccountNumber().equals(number)).findFirst();
    }

    public void addAccount(Account account) {
        accounts.add(account);
        saveAll();
    }

    public void updateAccount(Account account) {
        saveAll();
    }

    public void closeAccount(String accountId) {
        accounts.stream().filter(a -> a.getId().equals(accountId))
                .findFirst().ifPresent(a -> a.setActive(false));
        saveAll();
    }

    // ===================== TRANSACTION OPERATIONS =====================

    public List<Transaction> getAllTransactions() { return new ArrayList<>(transactions); }

    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactions.stream()
                .filter(t -> t.getAccountId().equals(accountId))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByUser(String userId) {
        return transactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        saveAll();
    }

    // ===================== PERSISTENCE =====================

    @SuppressWarnings("unchecked")
    private void loadAll() {
        users = loadFile(USERS_FILE);
        accounts = loadFile(ACCOUNTS_FILE);
        transactions = loadFile(TRANSACTIONS_FILE);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> loadFile(String path) {
        File file = new File(path);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void saveAll() {
        saveFile(USERS_FILE, users);
        saveFile(ACCOUNTS_FILE, accounts);
        saveFile(TRANSACTIONS_FILE, transactions);
    }

    private <T> void saveFile(String path, List<T> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(data);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
}
