package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.Beneficiary;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BeneficiaryDao {
    public void addBeneficiary(Beneficiary b) {
        String sql = "INSERT INTO beneficiaries (id, user_id, name, account_number, nickname, bank_note, added_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, b.getId());
            pstmt.setString(2, b.getUserId());
            pstmt.setString(3, b.getName());
            pstmt.setString(4, b.getAccountNumber());
            pstmt.setString(5, b.getNickname());
            pstmt.setString(6, b.getBankNote());
            pstmt.setString(7, b.getAddedAt() != null ? b.getAddedAt().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding beneficiary", e);
        }
    }

    public void removeBeneficiary(String id) {
        String sql = "DELETE FROM beneficiaries WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting beneficiary", e);
        }
    }

    public List<Beneficiary> findByUserId(String userId) {
        List<Beneficiary> list = new ArrayList<>();
        String sql = "SELECT * FROM beneficiaries WHERE user_id=? ORDER BY added_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToBeneficiary(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding beneficiaries by user_id", e);
        }
        return list;
    }

    public List<Beneficiary> findAll() {
        List<Beneficiary> list = new ArrayList<>();
        String sql = "SELECT * FROM beneficiaries ORDER BY added_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToBeneficiary(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all beneficiaries", e);
        }
        return list;
    }

    private Beneficiary mapRowToBeneficiary(ResultSet rs) throws SQLException {
        Beneficiary b = new Beneficiary(
            rs.getString("user_id"),
            rs.getString("name"),
            rs.getString("account_number"),
            rs.getString("nickname")
        );
        b.setBankNote(rs.getString("bank_note"));
        
        try {
            java.lang.reflect.Field idField = Beneficiary.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(b, rs.getString("id"));

            String addedAtStr = rs.getString("added_at");
            if (addedAtStr != null) {
                java.lang.reflect.Field aaField = Beneficiary.class.getDeclaredField("addedAt");
                aaField.setAccessible(true);
                aaField.set(b, LocalDateTime.parse(addedAtStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Beneficiary fields", e);
        }
        return b;
    }
}
