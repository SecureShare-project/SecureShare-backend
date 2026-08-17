package com.lakshith.secureshare.model;

public class PendingPasswordReset {
    private String email;
    private String otpCode;
    private long otpGeneratedAt;

    public PendingPasswordReset(String email, String otpCode, long otpGeneratedAt) {
        this.email = email;
        this.otpCode = otpCode;
        this.otpGeneratedAt = otpGeneratedAt;
    }

    public String getEmail() { return email; }
    public String getOtpCode() { return otpCode; }
    public long getOtpGeneratedAt() { return otpGeneratedAt; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public void setOtpGeneratedAt(long otpGeneratedAt) { this.otpGeneratedAt = otpGeneratedAt; }
}