package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    public void addUser(User user) {
        String sql = "INSERT INTO users (id, username, password, full_name, email, phone, address, role, credit_score, transaction_pin, otp_code, otp_expiry, created_at, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getAddress());
            pstmt.setString(8, user.getRole());
            pstmt.setInt(9, user.getCreditScore());
            pstmt.setString(10, user.getTransactionPin());
            pstmt.setString(11, user.getOtpCode());
            pstmt.setString(12, user.getOtpExpiry() != null ? user.getOtpExpiry().toString() : null);
            pstmt.setString(13, user.getCreatedAt().toString());
            pstmt.setInt(14, user.isActive() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding user", e);
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET username=?, password=?, full_name=?, email=?, phone=?, address=?, role=?, credit_score=?, transaction_pin=?, otp_code=?, otp_expiry=?, active=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getAddress());
            pstmt.setString(7, user.getRole());
            pstmt.setInt(8, user.getCreditScore());
            pstmt.setString(9, user.getTransactionPin());
            pstmt.setString(10, user.getOtpCode());
            pstmt.setString(11, user.getOtpExpiry() != null ? user.getOtpExpiry().toString() : null);
            pstmt.setInt(12, user.isActive() ? 1 : 0);
            pstmt.setString(13, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    public void deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
        return list;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        // Need to use reflection or custom constructor to set final/private fields like id, createdAt
        // but User only has one public constructor. We will construct it and use reflection to set ID/CreatedAt,
        // OR we can add package-private setters, OR just use reflection.
        User user = new User(
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("role")
        );
        user.setAddress(rs.getString("address"));
        
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, rs.getString("id"));
            
            java.lang.reflect.Field creditScoreField = User.class.getDeclaredField("creditScore");
            creditScoreField.setAccessible(true);
            creditScoreField.set(user, rs.getInt("credit_score"));
            
            java.lang.reflect.Field transactionPinField = User.class.getDeclaredField("transactionPin");
            transactionPinField.setAccessible(true);
            transactionPinField.set(user, rs.getString("transaction_pin"));
            
            String otpExpiry = rs.getString("otp_expiry");
            if (otpExpiry != null) {
                user.setOtpCode(rs.getString("otp_code"), LocalDateTime.parse(otpExpiry));
            }
            
            java.lang.reflect.Field createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, LocalDateTime.parse(rs.getString("created_at")));
        } catch (Exception e) {
            throw new RuntimeException("Failed to map User fields", e);
        }
        
        user.setActive(rs.getInt("active") == 1);
        return user;
    }
}
