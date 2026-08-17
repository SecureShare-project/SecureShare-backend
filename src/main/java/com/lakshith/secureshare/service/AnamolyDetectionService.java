package com.lakshith.secureshare.service;

import com.lakshith.secureshare.model.OtpPurpose;
import com.lakshith.secureshare.repository.AccessLogRepository;
import com.lakshith.secureshare.repository.OtpAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnamolyDetectionService {

    private static final int MAX_FAILED_OTP_ATTEMPTS = 5;
    private static final int OTP_WINDOW_MINUTES = 10;

    private static final int MAX_FAILED_SHARE_ATTEMPTS = 5;
    private static final int SHARE_WINDOW_MINUTES = 10;

    private final OtpAttemptRepository otpAttemptRepository;
    private final AccessLogRepository accessLogRepository;

    public AnamolyDetectionService(
            OtpAttemptRepository otpAttemptRepository,
            AccessLogRepository accessLogRepository) {
        this.otpAttemptRepository = otpAttemptRepository;
        this.accessLogRepository = accessLogRepository;
    }

    public boolean isOtpVerificationBlocked(String email, OtpPurpose purpose) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(OTP_WINDOW_MINUTES);

        long recentFailures = otpAttemptRepository
                .countByEmailAndPurposeAndSuccessfulFalseAndAttemptedAtAfter(email, purpose, windowStart);

        return recentFailures >= MAX_FAILED_OTP_ATTEMPTS;
    }

    public boolean isShareAccessBlocked(String shareToken) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(SHARE_WINDOW_MINUTES);
        long recentFailures = accessLogRepository
                .countByShareTokenAndSuccessfulFalseAndAccessedAtAfter(shareToken, windowStart);
        return recentFailures >= MAX_FAILED_SHARE_ATTEMPTS;
    }
}