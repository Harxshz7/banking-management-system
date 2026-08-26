package banking.data.dao;

import banking.data.DatabaseManager;
import banking.models.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    public void addNotification(Notification n) {
        String sql = "INSERT INTO notifications (id, user_id, message, type, created_at, read) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, n.getId());
            pstmt.setString(2, n.getUserId());
            pstmt.setString(3, n.getMessage());
            pstmt.setString(4, n.getType().name());
            pstmt.setString(5, n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
            pstmt.setInt(6, n.isRead() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding notification", e);
        }
    }

    public void updateNotification(Notification n) {
        String sql = "UPDATE notifications SET user_id=?, message=?, type=?, created_at=?, read=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, n.getUserId());
            pstmt.setString(2, n.getMessage());
            pstmt.setString(3, n.getType().name());
            pstmt.setString(4, n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
            pstmt.setInt(5, n.isRead() ? 1 : 0);
            pstmt.setString(6, n.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating notification", e);
        }
    }

    public List<Notification> findByUserId(String userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToNotification(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding notifications by user_id", e);
        }
        return list;
    }

    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToNotification(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all notifications", e);
        }
        return list;
    }

    private Notification mapRowToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification(
            rs.getString("user_id"),
            rs.getString("message"),
            Notification.NotificationType.valueOf(rs.getString("type"))
        );
        n.setRead(rs.getInt("read") == 1);
        
        try {
            java.lang.reflect.Field idField = Notification.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(n, rs.getString("id"));

            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                java.lang.reflect.Field caField = Notification.class.getDeclaredField("createdAt");
                caField.setAccessible(true);
                caField.set(n, LocalDateTime.parse(createdAtStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Notification fields", e);
        }
        return n;
    }
}
