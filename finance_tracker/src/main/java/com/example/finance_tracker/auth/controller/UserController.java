package com.example.finance_tracker.auth.controller;

import com.example.finance_tracker.auth.model.User;
import com.example.finance_tracker.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User currentUser = user.get();
            // Delete avatar file if exists
            if (currentUser.getAvatarUrl() != null && currentUser.getAvatarUrl().startsWith("/uploads/")) {
                try {
                    String fileName = currentUser.getAvatarUrl().substring("/uploads/".length());
                    Path filePath = Paths.get(uploadDir).resolve(fileName);
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    // Log error but continue with account deletion
                    System.err.println("Failed to delete avatar file: " + e.getMessage());
                }
            }
            
            userRepository.delete(currentUser);
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully."));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (file.isEmpty()) {
             return ResponseEntity.badRequest().body(Map.of("message", "File is empty."));
        }

        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Delete old avatar if exists
                if (user.getAvatarUrl() != null && user.getAvatarUrl().startsWith("/uploads/")) {
                    try {
                        String oldFileName = user.getAvatarUrl().substring("/uploads/".length());
                        Path oldFilePath = Paths.get(uploadDir).resolve(oldFileName);
                        Files.deleteIfExists(oldFilePath);
                    } catch (IOException e) {
                        System.err.println("Failed to delete old avatar file: " + e.getMessage());
                    }
                }
                
                String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                String extension = "";
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i);
                }
                
                String newFileName = UUID.randomUUID().toString() + extension;
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                try (var inputStream = file.getInputStream()) {
                    Path filePath = uploadPath.resolve(newFileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }

                String avatarUrl = "/uploads/" + newFileName;
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
                
                return ResponseEntity.ok(Map.of("message", "Avatar updated successfully.", "avatarUrl", avatarUrl));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to upload file."));
        }
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<?> removeAvatar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getAvatarUrl() != null) {
                // If it's a local file, delete it
                if (user.getAvatarUrl().startsWith("/uploads/")) {
                    try {
                        String fileName = user.getAvatarUrl().substring("/uploads/".length());
                        Path filePath = Paths.get(uploadDir).resolve(fileName);
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        System.err.println("Failed to delete avatar file: " + e.getMessage());
                    }
                }
                
                // Clear the URL in DB (works for both local and Google/external URLs)
                user.setAvatarUrl(null);
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Avatar removed successfully."));
            }
            return ResponseEntity.ok(Map.of("message", "No avatar to remove."));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
    }
}
