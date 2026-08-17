package com.lakshith.secureshare.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter @Setter @AllArgsConstructor
public class PendingSignUp {
    private final String username;
    private final String email;
    private final String passwordHash;
    private String otpCode;         // regenerated on resend, so not final
    private long otpGeneratedAt;    // epoch millis — used to check the 3.5-min expiry manually
}