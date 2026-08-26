package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.AuditLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {
    public void addAuditLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (id, user_id, action, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log.getId());
            pstmt.setString(2, log.getUserId());
            pstmt.setString(3, log.getAction());
            pstmt.setString(4, log.getTimestamp() != null ? log.getTimestamp().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding audit log", e);
        }
    }

    public List<AuditLog> findByUserId(String userId) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id=? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAuditLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs by user_id", e);
        }
        return list;
    }

    public List<AuditLog> findAll() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAuditLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all audit logs", e);
        }
        return list;
    }

    private AuditLog mapRowToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog(
            rs.getString("user_id"),
            rs.getString("action")
        );
        
        try {
            java.lang.reflect.Field idField = AuditLog.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(log, rs.getString("id"));

            String timestampStr = rs.getString("timestamp");
            if (timestampStr != null) {
                java.lang.reflect.Field tsField = AuditLog.class.getDeclaredField("timestamp");
                tsField.setAccessible(true);
                tsField.set(log, LocalDateTime.parse(timestampStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AuditLog fields", e);
        }
        return log;
    }
}
