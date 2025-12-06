package com.example.finance_tracker.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}