package com.example.finance_tracker.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private String role;
    
    // New fields for OAuth
    private String provider;
    private String providerId;

    // 2FA and Avatar
    private String twoFactorSecret;
    private boolean isTwoFactorEnabled;
    private String avatarUrl;
}
