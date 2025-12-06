package com.example.finance_tracker.auth.service;

import com.example.finance_tracker.auth.jwt.JwtUtils;
import com.example.finance_tracker.auth.model.User;
import com.example.finance_tracker.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret:}")
    private String githubClientSecret;

    public OAuthService(UserRepository userRepository, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public String processGoogleLogin(String idToken) {
        try {
            // 1. Verify Google Token
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Invalid Google Token");
            }

            JsonNode body = objectMapper.readTree(response.getBody());
            // Google's tokeninfo endpoint returns 'email' and 'sub' (subject/id)
            if (!body.has("email") || !body.has("sub")) {
                 throw new RuntimeException("Invalid Google Token response: missing email or sub");
            }

            String email = body.get("email").asText();
            String googleId = body.get("sub").asText();
            String name = body.has("name") ? body.get("name").asText() : email.split("@")[0];

            // 2. Find or Create User
            return processUser(email, googleId, "google", name);

        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Google Login Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Google Login Failed: " + e.getMessage());
        }
    }

    private String processUser(String email, String providerId, String provider, String username) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update provider info if missing (linking accounts logic can go here)
            if (user.getProvider() == null) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                userRepository.save(user);
            }
        } else {
            // Register new user
            user = new User();
            user.setEmail(email);

            // Ensure unique username
            String uniqueUsername = username;
            int counter = 1;
            while (userRepository.existsByUsername(uniqueUsername)) {
                uniqueUsername = username + counter;
                counter++;
            }
            user.setUsername(uniqueUsername);

            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setRole("USER");
            user.setPassword(null); // No password for OAuth
            userRepository.save(user);
        }

        // 3. Generate JWT
        // We need to load UserDetails to generate the token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        
        // Manually set authentication context
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtUtils.generateJwtToken(authentication);
    }
}
