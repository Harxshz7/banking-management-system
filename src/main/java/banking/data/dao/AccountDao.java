package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDao {
    public void addAccount(Account account) {
        String sql = "INSERT INTO accounts (id, account_number, user_id, type, balance, minimum_balance, interest_rate, daily_withdrawal_limit, today_withdrawn, last_reset_date, created_at, active, frozen, description, fd_maturity_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getId());
            pstmt.setString(2, account.getAccountNumber());
            pstmt.setString(3, account.getUserId());
            pstmt.setString(4, account.getType().name());
            pstmt.setDouble(5, account.getBalance());
            pstmt.setDouble(6, account.getMinimumBalance());
            pstmt.setDouble(7, account.getInterestRate());
            pstmt.setDouble(8, account.getDailyWithdrawalLimit());
            pstmt.setDouble(9, account.getTodayWithdrawn());
            
            try {
                java.lang.reflect.Field lrdField = Account.class.getDeclaredField("lastResetDate");
                lrdField.setAccessible(true);
                LocalDate lrd = (LocalDate) lrdField.get(account);
                pstmt.setString(10, lrd != null ? lrd.toString() : null);
            } catch (Exception e) {
                pstmt.setString(10, null);
            }
            
            pstmt.setString(11, account.getCreatedAt() != null ? account.getCreatedAt().toString() : null);
            pstmt.setInt(12, account.isActive() ? 1 : 0);
            pstmt.setInt(13, account.isFrozen() ? 1 : 0);
            pstmt.setString(14, account.getDescription());
            pstmt.setString(15, account.getFdMaturityDate() != null ? account.getFdMaturityDate().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding account", e);
        }
    }

    public void updateAccount(Account account) {
        String sql = "UPDATE accounts SET user_id=?, type=?, balance=?, minimum_balance=?, interest_rate=?, daily_withdrawal_limit=?, today_withdrawn=?, last_reset_date=?, active=?, frozen=?, description=?, fd_maturity_date=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getUserId());
            pstmt.setString(2, account.getType().name());
            pstmt.setDouble(3, account.getBalance());
            pstmt.setDouble(4, account.getMinimumBalance());
            pstmt.setDouble(5, account.getInterestRate());
            pstmt.setDouble(6, account.getDailyWithdrawalLimit());
            pstmt.setDouble(7, account.getTodayWithdrawn());
            
            try {
                java.lang.reflect.Field lrdField = Account.class.getDeclaredField("lastResetDate");
                lrdField.setAccessible(true);
                LocalDate lrd = (LocalDate) lrdField.get(account);
                pstmt.setString(8, lrd != null ? lrd.toString() : null);
            } catch (Exception e) {
                pstmt.setString(8, null);
            }
            
            pstmt.setInt(9, account.isActive() ? 1 : 0);
            pstmt.setInt(10, account.isFrozen() ? 1 : 0);
            pstmt.setString(11, account.getDescription());
            pstmt.setString(12, account.getFdMaturityDate() != null ? account.getFdMaturityDate().toString() : null);
            pstmt.setString(13, account.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating account", e);
        }
    }

    public void deleteAccount(String id) {
        String sql = "DELETE FROM accounts WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account", e);
        }
    }

    public Optional<Account> findById(String id) {
        String sql = "SELECT * FROM accounts WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by id", e);
        }
        return Optional.empty();
    }

    public Optional<Account> findByNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by number", e);
        }
        return Optional.empty();
    }

    public List<Account> findByUserId(String userId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding accounts by user_id", e);
        }
        return list;
    }

    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all accounts", e);
        }
        return list;
    }

    private Account mapRowToAccount(ResultSet rs) throws SQLException {
        Account account = new Account(
            rs.getString("user_id"),
            Account.AccountType.valueOf(rs.getString("type")),
            rs.getDouble("balance"),
            rs.getString("account_number")
        );
        
        try {
            java.lang.reflect.Field idField = Account.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(account, rs.getString("id"));

            account.setMinimumBalance(rs.getDouble("minimum_balance"));
            account.setInterestRate(rs.getDouble("interest_rate"));
            account.setDailyWithdrawalLimit(rs.getDouble("daily_withdrawal_limit"));
            
            java.lang.reflect.Field twField = Account.class.getDeclaredField("todayWithdrawn");
            twField.setAccessible(true);
            twField.set(account, rs.getDouble("today_withdrawn"));
            
            String lastResetDateStr = rs.getString("last_reset_date");
            if (lastResetDateStr != null) {
                java.lang.reflect.Field lrdField = Account.class.getDeclaredField("lastResetDate");
                lrdField.setAccessible(true);
                lrdField.set(account, LocalDate.parse(lastResetDateStr));
            }
            
            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                java.lang.reflect.Field caField = Account.class.getDeclaredField("createdAt");
                caField.setAccessible(true);
                caField.set(account, LocalDateTime.parse(createdAtStr));
            }
            
            account.setActive(rs.getInt("active") == 1);
            account.setFrozen(rs.getInt("frozen") == 1);
            account.setDescription(rs.getString("description"));
            
            String fdMaturityStr = rs.getString("fd_maturity_date");
            if (fdMaturityStr != null) {
                account.setFdMaturityDate(LocalDateTime.parse(fdMaturityStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Account fields", e);
        }
        
        return account;
    }
}
