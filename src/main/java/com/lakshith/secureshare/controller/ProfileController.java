package com.lakshith.secureshare.controller;

import com.lakshith.secureshare.dto.*;
import com.lakshith.secureshare.model.User;
import com.lakshith.secureshare.repository.UserRepository;
import com.lakshith.secureshare.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    public ProfileController(ProfileService profileService, UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {
        profileService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @PutMapping("/username")
    public ResponseEntity<String> updateUsername(
            @Valid @RequestBody UpdateUsernameRequest request,
            Authentication authentication) {
        profileService.changeUsername(authentication.getName(), request.newUsername());
        return ResponseEntity.ok("Username updated successfully");
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        profileService.deleteAccount(authentication.getName());
        return ResponseEntity.ok("Account deleted successfully");
    }
}
