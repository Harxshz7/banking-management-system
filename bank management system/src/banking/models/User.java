package banking.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class User implements Serializable {
    private static final long serialVersionUID = 2L;

    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role; // "ADMIN", "EMPLOYEE", or "CUSTOMER"
    private int creditScore;
    private String transactionPin; // 4-digit PIN
    private String otpCode;
    private LocalDateTime otpExpiry;
    private LocalDateTime createdAt;
    private boolean active;

    public User(String username, String password, String fullName, String email, String phone, String role) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = "";
        this.role = role;
        this.creditScore = 680;
        this.transactionPin = null;
        this.otpCode = null;
        this.otpExpiry = null;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address == null ? "" : address;
    }

    public String getRole() {
        return role;
    }

    public String getTransactionPin() {
        return transactionPin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    // Setters
    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTransactionPin(String pin) {
        this.transactionPin = pin;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isEmployee() {
        return "EMPLOYEE".equals(role);
    }

    public boolean hasPinSet() {
        return transactionPin != null && !transactionPin.isEmpty();
    }

    public boolean verifyPin(String pin) {
        return transactionPin != null && transactionPin.equals(pin);
    }

    public boolean verifyOtp(String otp) {
        return otpCode != null && otpExpiry != null && LocalDateTime.now().isBefore(otpExpiry) && otpCode.equals(otp);
    }

    public void setOtpCode(String otp, LocalDateTime expiry) {
        this.otpCode = otp;
        this.otpExpiry = expiry;
    }

    public void updateCreditScore(int delta) {
        this.creditScore = Math.max(300, Math.min(850, this.creditScore + delta));
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
