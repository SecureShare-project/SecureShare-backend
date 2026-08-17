package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.AccessType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateFileShareRequest(
        @NotNull AccessType accessType,
        String password,
        @Positive long expiryMinutes
) {}
