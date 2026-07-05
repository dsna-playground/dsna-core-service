package com.dsa.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsa.core.dto.ProfileRequest;
import com.dsa.core.model.Profile;
import com.dsa.core.model.User;
import com.dsa.core.repository.UserRepository;
import com.dsa.core.service.ProfileServiceInterface;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
	
    @Autowired
    private ProfileServiceInterface profileService;
    private final UserRepository userRepository;
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // Retrieve the User object from your database based on the username
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<String> createProfile(@RequestBody ProfileRequest profileRequest) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in!");
        }
        try {
            profileService.createProfile(userId, profileRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("Profile created successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create profile: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in!");
        }
        Profile profile = profileService.getProfileByUserId(userId);
        if (profile != null) {
            return ResponseEntity.ok(profile);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile not found!");
        }
    }

    @PutMapping
    public ResponseEntity<String> updateProfile(@RequestBody ProfileRequest profileRequest) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in!");
        }
        try {
            profileService.updateProfile(userId, profileRequest);
            return ResponseEntity.ok("Profile updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update profile: " + e.getMessage());
        }
    }

    // We'll skip delete for now to keep it focused
}
