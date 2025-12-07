package com.example.finance_tracker.auth.controller;

import com.example.finance_tracker.auth.dto.*;
import com.example.finance_tracker.auth.jwt.JwtUtils;
import com.example.finance_tracker.auth.model.PasswordResetToken;
import com.example.finance_tracker.auth.model.User;
import com.example.finance_tracker.auth.repository.PasswordResetRepository;
import com.example.finance_tracker.auth.repository.UserRepository;
import com.example.finance_tracker.auth.service.AuthService;
import com.example.finance_tracker.auth.service.EmailService;
import com.example.finance_tracker.auth.service.OAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.example.finance_tracker.auth.service.UserDetailsServiceImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
    private final AuthService authService;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, 
                          UserRepository userRepository, 
                          PasswordEncoder encoder, 
                          JwtUtils jwtUtils, 
                          OAuthService oAuthService,
                          PasswordResetRepository passwordResetRepository,
                          EmailService emailService,
                          AuthService authService,
                          UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.oAuthService = oAuthService;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = emailService;
        this.authService = authService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Username is already taken!"));
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());
        user.setProvider("local"); 

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        org.springframework.security.core.userdetails.User springUser = 
            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        
        User user = userRepository.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isTwoFactorEnabled()) {
             return ResponseEntity.ok(Map.of("requires2fa", true, "username", user.getUsername()));
        } else {
             // Force setup
             if (user.getTwoFactorSecret() == null) {
                 user.setTwoFactorSecret(authService.generate2faSecret());
                 userRepository.save(user);
             }
             String qrUrl = "otpauth://totp/FinanceTracker:" + user.getUsername() + "?secret=" + user.getTwoFactorSecret() + "&issuer=FinanceTracker";
             return ResponseEntity.ok(Map.of(
                 "setup2fa", true, 
                 "secret", user.getTwoFactorSecret(), 
                 "qrUrl", qrUrl, 
                 "username", user.getUsername()
             ));
        }
    }
    
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2fa(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String codeStr = request.get("code").toString();
        int code = Integer.parseInt(codeStr);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (authService.verify2fa(user.getTwoFactorSecret(), code)) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            return ResponseEntity.ok(Map.of("message", "Login successful!", "token", jwt));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid 2FA Code"));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<?> getAuthConfig() {
        return ResponseEntity.ok(Map.of("googleClientId", googleClientId));
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> authenticateOAuth(@RequestBody OAuthLoginRequest oauthRequest) {
        Map<String, Object> oauthResult;
        if ("google".equalsIgnoreCase(oauthRequest.getProvider())) {
            oauthResult = oAuthService.processGoogleLogin(oauthRequest.getToken());
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid provider"));
        }
        
        User user = (User) oauthResult.get("user");

        if (user.isTwoFactorEnabled()) {
             return ResponseEntity.ok(Map.of("requires2fa", true, "username", user.getUsername()));
        } else {
             // Force setup
             if (user.getTwoFactorSecret() == null) {
                 user.setTwoFactorSecret(authService.generate2faSecret());
                 userRepository.save(user);
             }
             String qrUrl = "otpauth://totp/FinanceTracker:" + user.getUsername() + "?secret=" + user.getTwoFactorSecret() + "&issuer=FinanceTracker";
             return ResponseEntity.ok(Map.of(
                 "setup2fa", true, 
                 "secret", user.getTwoFactorSecret(), 
                 "qrUrl", qrUrl, 
                 "username", user.getUsername()
             ));
        }
    }
    
    @PostMapping("/verify-2fa-oauth")
    public ResponseEntity<?> verify2faOAuth(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String codeStr = request.get("code").toString();
        int code = Integer.parseInt(codeStr);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (authService.verify2fa(user.getTwoFactorSecret(), code)) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            
             // Generate JWT for OAuth User
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);
            return ResponseEntity.ok(Map.of("message", "Login successful!", "token", jwt));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid 2FA Code"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Email not found!"));
        }

        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        passwordResetRepository.deleteByEmail(email);
        PasswordResetToken resetToken = new PasswordResetToken(email, token, LocalDateTime.now().plusMinutes(15));
        passwordResetRepository.save(resetToken);

        emailService.sendPasswordResetEmail(email, token);

        return ResponseEntity.ok(Map.of("message", "Reset code sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
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

        passwordResetRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of("message", "Password successfully reset!"));
    }
}
