package com.example.finance_tracker.auth.controller;

import com.example.finance_tracker.auth.dto.*;
import com.example.finance_tracker.auth.jwt.JwtUtils;
import com.example.finance_tracker.auth.model.PasswordResetToken;
import com.example.finance_tracker.auth.model.User;
import com.example.finance_tracker.auth.repository.PasswordResetRepository;
import com.example.finance_tracker.auth.repository.UserRepository;
import com.example.finance_tracker.auth.service.EmailService;
import com.example.finance_tracker.auth.service.OAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final OAuthService oAuthService;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;

    // Use constructor injection for all dependencies
    public AuthController(AuthenticationManager authenticationManager, 
                          UserRepository userRepository, 
                          PasswordEncoder encoder, 
                          JwtUtils jwtUtils, 
                          OAuthService oAuthService,
                          PasswordResetRepository passwordResetRepository,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.oAuthService = oAuthService;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Username is already taken!"));
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        //Encode the password before saving
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());
        user.setProvider("local"); // Default provider

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Return the JWT token in the response
        return ResponseEntity.ok(Map.of(
                "message", "Login successful!",
                "token", jwt
        ));
    }

    @GetMapping("/config")
    public ResponseEntity<?> getAuthConfig() {
        return ResponseEntity.ok(Map.of("googleClientId", googleClientId));
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> authenticateOAuth(@RequestBody OAuthLoginRequest oauthRequest) {
        String jwt;
        if ("google".equalsIgnoreCase(oauthRequest.getProvider())) {
            jwt = oAuthService.processGoogleLogin(oauthRequest.getToken());
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid provider"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful!",
                "token", jwt
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Email not found!"));
        }

        // Generate 6-digit numeric code
        String token = String.format("%06d", new java.util.Random().nextInt(999999));

        // Delete existing token if any
        passwordResetRepository.deleteByEmail(email);

        PasswordResetToken resetToken = new PasswordResetToken(email, token, LocalDateTime.now().plusMinutes(15));
        passwordResetRepository.save(resetToken);

        emailService.sendPasswordResetEmail(email, token);

        return ResponseEntity.ok(Map.of("message", "Reset code sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOptional = passwordResetRepository.findByToken(request.getToken());

        if (tokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Invalid reset code!"));
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.isExpired()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Reset code expired!"));
        }

        if (!resetToken.getEmail().equals(request.getEmail())) {
             return ResponseEntity.badRequest().body(Map.of("message", "Error: Email doesn't match the code!"));
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: User not found!"));
        }

        User user = userOptional.get();
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete token after successful reset
        passwordResetRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of("message", "Password successfully reset!"));
    }
}
