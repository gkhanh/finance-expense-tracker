package com.example.finance_tracker.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "two_factor_setup_tokens")
public class TwoFactorSetupToken {
    @Id
    private String id;
    private String username;
    private String email;
    private String token;
    private LocalDateTime expiryDate;

    public TwoFactorSetupToken() {}

    public TwoFactorSetupToken(String username, String email, String token, LocalDateTime expiryDate) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
