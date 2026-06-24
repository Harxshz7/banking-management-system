package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Beneficiary implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;          // Owner of this beneficiary entry
    private String name;            // Beneficiary's name
    private String accountNumber;   // Their account number
    private String nickname;        // User-assigned nickname
    private String bankNote;        // Optional note
    private LocalDateTime addedAt;

    public Beneficiary(String userId, String name, String accountNumber, String nickname) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.accountNumber = accountNumber;
        this.nickname = nickname;
        this.bankNote = "";
        this.addedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public String getNickname() { return nickname; }
    public String getBankNote() { return bankNote; }
    public LocalDateTime getAddedAt() { return addedAt; }

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setBankNote(String note) { this.bankNote = note; }

    public String getDisplayName() {
        return nickname.isEmpty() ? name : nickname + " (" + name + ")";
    }

    @Override
    public String toString() {
        return getDisplayName() + " — " + accountNumber;
    }
}
