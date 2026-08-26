package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.ScheduledTransfer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduledTransferDao {
    public void addScheduledTransfer(ScheduledTransfer st) {
        String sql = "INSERT INTO scheduled_transfers (id, user_id, from_account_id, to_account_number, amount, description, scheduled_date, recurring_monthly, executed, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, st.getId());
            pstmt.setString(2, st.getUserId());
            pstmt.setString(3, st.getFromAccountId());
            pstmt.setString(4, st.getToAccountNumber());
            pstmt.setDouble(5, st.getAmount());
            pstmt.setString(6, st.getDescription());
            pstmt.setString(7, st.getScheduledDate() != null ? st.getScheduledDate().toString() : null);
            pstmt.setInt(8, st.isRecurringMonthly() ? 1 : 0);
            pstmt.setInt(9, st.isExecuted() ? 1 : 0);
            pstmt.setString(10, st.getCreatedAt() != null ? st.getCreatedAt().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding scheduled transfer", e);
        }
    }

    public void updateScheduledTransfer(ScheduledTransfer st) {
        String sql = "UPDATE scheduled_transfers SET user_id=?, from_account_id=?, to_account_number=?, amount=?, description=?, scheduled_date=?, recurring_monthly=?, executed=?, created_at=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, st.getUserId());
            pstmt.setString(2, st.getFromAccountId());
            pstmt.setString(3, st.getToAccountNumber());
            pstmt.setDouble(4, st.getAmount());
            pstmt.setString(5, st.getDescription());
            pstmt.setString(6, st.getScheduledDate() != null ? st.getScheduledDate().toString() : null);
            pstmt.setInt(7, st.isRecurringMonthly() ? 1 : 0);
            pstmt.setInt(8, st.isExecuted() ? 1 : 0);
            pstmt.setString(9, st.getCreatedAt() != null ? st.getCreatedAt().toString() : null);
            pstmt.setString(10, st.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating scheduled transfer", e);
        }
    }

    public void removeScheduledTransfer(String id) {
        String sql = "DELETE FROM scheduled_transfers WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting scheduled transfer", e);
        }
    }

    public List<ScheduledTransfer> findByUserId(String userId) {
        List<ScheduledTransfer> list = new ArrayList<>();
        String sql = "SELECT * FROM scheduled_transfers WHERE user_id=? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToScheduledTransfer(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding scheduled transfers by user_id", e);
        }
        return list;
    }

    public List<ScheduledTransfer> findAll() {
        List<ScheduledTransfer> list = new ArrayList<>();
        String sql = "SELECT * FROM scheduled_transfers ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToScheduledTransfer(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all scheduled transfers", e);
        }
        return list;
    }

    private ScheduledTransfer mapRowToScheduledTransfer(ResultSet rs) throws SQLException {
        ScheduledTransfer st = new ScheduledTransfer(
            rs.getString("user_id"),
            rs.getString("from_account_id"),
            rs.getString("to_account_number"),
            rs.getDouble("amount"),
            rs.getString("description"),
            LocalDate.parse(rs.getString("scheduled_date")),
            rs.getInt("recurring_monthly") == 1
        );
        
        try {
            java.lang.reflect.Field idField = ScheduledTransfer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(st, rs.getString("id"));

            java.lang.reflect.Field execField = ScheduledTransfer.class.getDeclaredField("executed");
            execField.setAccessible(true);
            execField.set(st, rs.getInt("executed") == 1);

            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                java.lang.reflect.Field caField = ScheduledTransfer.class.getDeclaredField("createdAt");
                caField.setAccessible(true);
                caField.set(st, LocalDateTime.parse(createdAtStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ScheduledTransfer fields", e);
        }
        return st;
    }
}
