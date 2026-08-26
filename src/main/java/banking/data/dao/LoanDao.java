package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.Loan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDao {
    public void addLoan(Loan loan) {
        String sql = "INSERT INTO loans (id, user_id, credit_account_id, type, principal_amount, interest_rate, tenure_months, emi_amount, total_payable, amount_paid, status, purpose, applied_at, processed_at, admin_note, months_paid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loan.getId());
            pstmt.setString(2, loan.getUserId());
            pstmt.setString(3, loan.getCreditAccountId());
            pstmt.setString(4, loan.getType().name());
            pstmt.setDouble(5, loan.getPrincipalAmount());
            pstmt.setDouble(6, loan.getInterestRate());
            pstmt.setInt(7, loan.getTenureMonths());
            pstmt.setDouble(8, loan.getEmiAmount());
            pstmt.setDouble(9, loan.getTotalPayable());
            pstmt.setDouble(10, loan.getAmountPaid());
            pstmt.setString(11, loan.getStatus().name());
            pstmt.setString(12, loan.getPurpose());
            pstmt.setString(13, loan.getAppliedAt() != null ? loan.getAppliedAt().toString() : null);
            pstmt.setString(14, loan.getProcessedAt() != null ? loan.getProcessedAt().toString() : null);
            pstmt.setString(15, loan.getAdminNote());
            pstmt.setInt(16, loan.getMonthsPaid());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding loan", e);
        }
    }

    public void updateLoan(Loan loan) {
        String sql = "UPDATE loans SET user_id=?, credit_account_id=?, type=?, principal_amount=?, interest_rate=?, tenure_months=?, emi_amount=?, total_payable=?, amount_paid=?, status=?, purpose=?, processed_at=?, admin_note=?, months_paid=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loan.getUserId());
            pstmt.setString(2, loan.getCreditAccountId());
            pstmt.setString(3, loan.getType().name());
            pstmt.setDouble(4, loan.getPrincipalAmount());
            pstmt.setDouble(5, loan.getInterestRate());
            pstmt.setInt(6, loan.getTenureMonths());
            pstmt.setDouble(7, loan.getEmiAmount());
            pstmt.setDouble(8, loan.getTotalPayable());
            pstmt.setDouble(9, loan.getAmountPaid());
            pstmt.setString(10, loan.getStatus().name());
            pstmt.setString(11, loan.getPurpose());
            pstmt.setString(12, loan.getProcessedAt() != null ? loan.getProcessedAt().toString() : null);
            pstmt.setString(13, loan.getAdminNote());
            pstmt.setInt(14, loan.getMonthsPaid());
            pstmt.setString(15, loan.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating loan", e);
        }
    }

    public Optional<Loan> findById(String id) {
        String sql = "SELECT * FROM loans WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding loan by id", e);
        }
        return Optional.empty();
    }

    public List<Loan> findByUserId(String userId) {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE user_id=? ORDER BY applied_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding loans by user_id", e);
        }
        return list;
    }

    public List<Loan> findAll() {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT * FROM loans ORDER BY applied_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all loans", e);
        }
        return list;
    }

    private Loan mapRowToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan(
            rs.getString("user_id"),
            rs.getString("credit_account_id"),
            Loan.LoanType.valueOf(rs.getString("type")),
            rs.getDouble("principal_amount"),
            rs.getInt("tenure_months"),
            rs.getString("purpose")
        );
        
        try {
            java.lang.reflect.Field idField = Loan.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(loan, rs.getString("id"));

            java.lang.reflect.Field irField = Loan.class.getDeclaredField("interestRate");
            irField.setAccessible(true);
            irField.set(loan, rs.getDouble("interest_rate"));

            java.lang.reflect.Field emiField = Loan.class.getDeclaredField("emiAmount");
            emiField.setAccessible(true);
            emiField.set(loan, rs.getDouble("emi_amount"));

            java.lang.reflect.Field tpField = Loan.class.getDeclaredField("totalPayable");
            tpField.setAccessible(true);
            tpField.set(loan, rs.getDouble("total_payable"));

            java.lang.reflect.Field apField = Loan.class.getDeclaredField("amountPaid");
            apField.setAccessible(true);
            apField.set(loan, rs.getDouble("amount_paid"));

            java.lang.reflect.Field statusField = Loan.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(loan, Loan.LoanStatus.valueOf(rs.getString("status")));

            String appliedAtStr = rs.getString("applied_at");
            if (appliedAtStr != null) {
                java.lang.reflect.Field aaField = Loan.class.getDeclaredField("appliedAt");
                aaField.setAccessible(true);
                aaField.set(loan, LocalDateTime.parse(appliedAtStr));
            }

            String processedAtStr = rs.getString("processed_at");
            if (processedAtStr != null) {
                java.lang.reflect.Field paField = Loan.class.getDeclaredField("processedAt");
                paField.setAccessible(true);
                paField.set(loan, LocalDateTime.parse(processedAtStr));
            }

            java.lang.reflect.Field noteField = Loan.class.getDeclaredField("adminNote");
            noteField.setAccessible(true);
            noteField.set(loan, rs.getString("admin_note"));

            java.lang.reflect.Field mpField = Loan.class.getDeclaredField("monthsPaid");
            mpField.setAccessible(true);
            mpField.set(loan, rs.getInt("months_paid"));

        } catch (Exception e) {
            throw new RuntimeException("Failed to map Loan fields", e);
        }
        
        return loan;
    }
}
