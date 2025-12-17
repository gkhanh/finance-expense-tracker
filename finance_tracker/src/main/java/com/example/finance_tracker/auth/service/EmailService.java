package com.example.finance_tracker.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// For test via Mailtrap Sandbox
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Fixed "From" address so it looks like a real email
    private String fromEmail = "noreply@financetracker.com";

    public void sendPasswordResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, please use the following code:\n\n" + token + "\n\nThis code will expire in 15 minutes.");
        
        mailSender.send(message);
    }

    public void send2FASetupEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("2FA Setup Code - Finance Tracker");
        message.setText("To complete your 2FA setup, please use the following code:\n\n" + token + "\n\nThis code will expire in 10 minutes.\n\nIf you did not request this code, please ignore this email.");
        
        mailSender.send(message);
    }
}
