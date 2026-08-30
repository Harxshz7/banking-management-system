package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    public void addTransaction(Transaction tx) {
        String sql = "INSERT OR REPLACE INTO transactions (id, receipt_number, account_id, user_id, type, amount, balance_after, description, related_account_id, timestamp, channel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tx.getId());
            pstmt.setString(2, tx.getReceiptNumber());
            pstmt.setString(3, tx.getAccountId());
            pstmt.setString(4, tx.getUserId());
            pstmt.setString(5, tx.getType().name());
            pstmt.setDouble(6, tx.getAmount());
            pstmt.setDouble(7, tx.getBalanceAfter());
            pstmt.setString(8, tx.getDescription());
            pstmt.setString(9, tx.getRelatedAccountId());
            pstmt.setString(10, tx.getTimestamp() != null ? tx.getTimestamp().toString() : null);
            pstmt.setString(11, tx.getChannel());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding transaction", e);
        }
    }

    public List<Transaction> findByAccountId(String accountId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id=? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToTransaction(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transactions by account_id", e);
        }
        return list;
    }

    public List<Transaction> findByUserId(String userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id=? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToTransaction(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transactions by user_id", e);
        }
        return list;
    }

    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToTransaction(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all transactions", e);
        }
        return list;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction(
            rs.getString("account_id"),
            rs.getString("user_id"),
            Transaction.TransactionType.valueOf(rs.getString("type")),
            rs.getDouble("amount"),
            rs.getDouble("balance_after"),
            rs.getString("description"),
            rs.getString("related_account_id")
        );
        tx.setChannel(rs.getString("channel"));
        
        try {
            java.lang.reflect.Field idField = Transaction.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(tx, rs.getString("id"));

            java.lang.reflect.Field receiptField = Transaction.class.getDeclaredField("receiptNumber");
            receiptField.setAccessible(true);
            receiptField.set(tx, rs.getString("receipt_number"));

            String timestampStr = rs.getString("timestamp");
            if (timestampStr != null) {
                java.lang.reflect.Field tsField = Transaction.class.getDeclaredField("timestamp");
                tsField.setAccessible(true);
                tsField.set(tx, LocalDateTime.parse(timestampStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Transaction fields", e);
        }
        
        return tx;
    }
}
