package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.RecurringDeposit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecurringDepositDao {
    public void addRecurringDeposit(RecurringDeposit rd) {
        String sql = "INSERT INTO recurring_deposits (id, user_id, account_id, monthly_amount, months, interest_rate, start_date, maturity_date, accumulated_amount, active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rd.getId());
            pstmt.setString(2, rd.getUserId());
            pstmt.setString(3, rd.getAccountId());
            pstmt.setDouble(4, rd.getMonthlyAmount());
            pstmt.setInt(5, rd.getMonths());
            pstmt.setDouble(6, rd.getInterestRate());
            pstmt.setString(7, rd.getStartDate() != null ? rd.getStartDate().toString() : null);
            pstmt.setString(8, rd.getMaturityDate() != null ? rd.getMaturityDate().toString() : null);
            pstmt.setDouble(9, rd.getAccumulatedAmount());
            pstmt.setInt(10, rd.isActive() ? 1 : 0);
            pstmt.setString(11, rd.getCreatedAt() != null ? rd.getCreatedAt().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding recurring deposit", e);
        }
    }

    public void updateRecurringDeposit(RecurringDeposit rd) {
        String sql = "UPDATE recurring_deposits SET user_id=?, account_id=?, monthly_amount=?, months=?, interest_rate=?, start_date=?, maturity_date=?, accumulated_amount=?, active=?, created_at=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rd.getUserId());
            pstmt.setString(2, rd.getAccountId());
            pstmt.setDouble(3, rd.getMonthlyAmount());
            pstmt.setInt(4, rd.getMonths());
            pstmt.setDouble(5, rd.getInterestRate());
            pstmt.setString(6, rd.getStartDate() != null ? rd.getStartDate().toString() : null);
            pstmt.setString(7, rd.getMaturityDate() != null ? rd.getMaturityDate().toString() : null);
            pstmt.setDouble(8, rd.getAccumulatedAmount());
            pstmt.setInt(9, rd.isActive() ? 1 : 0);
            pstmt.setString(10, rd.getCreatedAt() != null ? rd.getCreatedAt().toString() : null);
            pstmt.setString(11, rd.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating recurring deposit", e);
        }
    }

    public List<RecurringDeposit> findByUserId(String userId) {
        List<RecurringDeposit> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_deposits WHERE user_id=? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToRecurringDeposit(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding recurring deposits by user_id", e);
        }
        return list;
    }

    public List<RecurringDeposit> findAll() {
        List<RecurringDeposit> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_deposits ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToRecurringDeposit(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all recurring deposits", e);
        }
        return list;
    }

    private RecurringDeposit mapRowToRecurringDeposit(ResultSet rs) throws SQLException {
        RecurringDeposit rd = new RecurringDeposit(
            rs.getString("user_id"),
            rs.getString("account_id"),
            rs.getDouble("monthly_amount"),
            rs.getInt("months"),
            rs.getDouble("interest_rate"),
            LocalDate.parse(rs.getString("start_date"))
        );
        
        try {
            java.lang.reflect.Field idField = RecurringDeposit.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rd, rs.getString("id"));

            java.lang.reflect.Field accField = RecurringDeposit.class.getDeclaredField("accumulatedAmount");
            accField.setAccessible(true);
            accField.set(rd, rs.getDouble("accumulated_amount"));
            
            java.lang.reflect.Field activeField = RecurringDeposit.class.getDeclaredField("active");
            activeField.setAccessible(true);
            activeField.set(rd, rs.getInt("active") == 1);

            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                java.lang.reflect.Field caField = RecurringDeposit.class.getDeclaredField("createdAt");
                caField.setAccessible(true);
                caField.set(rd, LocalDateTime.parse(createdAtStr));
            }
            
            String maturityStr = rs.getString("maturity_date");
            if (maturityStr != null) {
                java.lang.reflect.Field mdField = RecurringDeposit.class.getDeclaredField("maturityDate");
                mdField.setAccessible(true);
                mdField.set(rd, LocalDate.parse(maturityStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map RecurringDeposit fields", e);
        }
        return rd;
    }
}
