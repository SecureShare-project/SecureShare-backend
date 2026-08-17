package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.AuthProvider;
import com.lakshith.secureshare.model.User;

import java.time.LocalDateTime;

public record UserProfileResponse(
        String username,
        String email,
        AuthProvider authProvider,
        LocalDateTime createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getAuthProvider(),
                user.getCreatedAt()
        );
    }
}