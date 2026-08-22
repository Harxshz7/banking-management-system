package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum NotificationType {
        INFO, SUCCESS, WARNING, ALERT
    }

    private String id;
    private String userId;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean read;

    public Notification(String userId, String message, NotificationType type) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getFormattedTimestamp() {
        return createdAt.toString().replace('T', ' ');
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", type.name(), message);
    }
}
