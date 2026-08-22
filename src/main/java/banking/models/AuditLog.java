package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String action;
    private LocalDateTime timestamp;

    public AuditLog(String userId, String action) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.toString().replace('T', ' ');
    }
}
