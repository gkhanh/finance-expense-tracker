package com.example.finance_tracker.auth.dto;

import lombok.Data;

@Data
public class OAuthLoginRequest {
    private String token; // For Google (ID Token)
    private String provider; // "google" or "github"
}
