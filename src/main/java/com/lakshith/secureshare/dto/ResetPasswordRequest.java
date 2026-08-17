package com.lakshith.secureshare.dto;

public record ResetPasswordRequest(String email, String otp, String newPassword) {}