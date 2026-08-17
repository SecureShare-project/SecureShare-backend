package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.AccessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTextShareRequest(
        @NotBlank String textContent,
        @NotNull AccessType accessType,
        String password, // required only if accessType == PASSWORD
        @Positive long expiryMinutes
) {}
