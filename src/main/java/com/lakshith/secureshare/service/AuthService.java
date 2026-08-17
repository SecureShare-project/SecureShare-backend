package com.lakshith.secureshare.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.lakshith.secureshare.dto.*;
import com.lakshith.secureshare.exception.InvalidOtpException;
import com.lakshith.secureshare.model.*;
import com.lakshith.secureshare.repository.LoginAttemptRepository;
import com.lakshith.secureshare.repository.OtpAttemptRepository;
import com.lakshith.secureshare.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Duration OTP_VALIDITY = Duration.ofMinutes(3).plusSeconds(30); // 3.5 min
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_EMAIL_FAILURES = 5;
    private static final int MAX_IP_FAILURES = 20;
    private static final int WINDOW_MINUTES = 15;

    private final Cache<String, PendingSignUp> pendingSignupCache;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpAttemptRepository otpAttemptRepository;
    private final AnamolyDetectionService anamolyDetectionService;
    private final LoginAttemptRepository loginAttemptRepository;
    private final Cache<String, PendingPasswordReset> pendingPasswordResetCache;
// add to constructor param list + field assignment

    public AuthService(
            Cache<String, PendingSignUp> pendingSignupCache,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService,
            OtpAttemptRepository otpAttemptRepository,
            AnamolyDetectionService anamolyDetectionService,
            LoginAttemptRepository loginAttemptRepository,
            Cache pendingPasswordResetCache) {
        this.pendingSignupCache = pendingSignupCache;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.otpAttemptRepository = otpAttemptRepository;
        this.anamolyDetectionService = anamolyDetectionService;
        this.loginAttemptRepository = loginAttemptRepository;
        this.pendingPasswordResetCache = pendingPasswordResetCache;
    }

    public void initiateSignup(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }

        String otp = generateOtp();
        String passwordHash = passwordEncoder.encode(request.password());

        emailService.sendOtpEmail(request.email(), otp);

        PendingSignUp pending = new PendingSignUp(
                request.username(),
                request.email(),
                passwordHash,
                otp,
                System.currentTimeMillis()
        );

        pendingSignupCache.put(request.email(), pending);

        // TODO: wire in EmailService to actually send `otp` to request.email()
    }

    public void resendOtp(ResendOtpRequest request) {
        PendingSignUp pending = pendingSignupCache.getIfPresent(request.email());
        if (pending == null) {
            throw new IllegalArgumentException("No pending signup found for this email");
        }

        pending.setOtpCode(generateOtp());
        pending.setOtpGeneratedAt(System.currentTimeMillis());
        pendingSignupCache.put(request.email(), pending);

        emailService.sendOtpEmail(request.email(), pending.getOtpCode());

        // TODO: wire in EmailService to resend `pending.getOtpCode()`
    }

    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        if (anamolyDetectionService.isOtpVerificationBlocked(request.email(), OtpPurpose.SIGNUP)) {
            throw new IllegalArgumentException("Too many failed attempts. Please try again later.");
        }

        PendingSignUp pending = pendingSignupCache.getIfPresent(request.email());
        if (pending == null) {
            logOtpAttempt(request.email(),OtpPurpose.SIGNUP ,false);
            throw new IllegalArgumentException("No pending signup found for this email");
        }

        boolean expired = System.currentTimeMillis() - pending.getOtpGeneratedAt() > OTP_VALIDITY.toMillis();
        if (expired) {
            pendingSignupCache.invalidate(request.email());
            logOtpAttempt(request.email(), OtpPurpose.SIGNUP,false);
            throw new IllegalArgumentException("OTP has expired, please request a new one");
        }

        if (!pending.getOtpCode().equals(request.otpCode())) {
            logOtpAttempt(request.email(), OtpPurpose.SIGNUP,false);
            throw new InvalidOtpException("Invalid OTP");
        }

        User user = new User(
                null,
                pending.getUsername(),
                pending.getEmail(),
                pending.getPasswordHash(),
                AuthProvider.LOCAL,
                true,
                LocalDateTime.now()
        );
        userRepository.save(user);
        pendingSignupCache.invalidate(request.email());
        logOtpAttempt(request.email(), OtpPurpose.SIGNUP,true);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    private void logOtpAttempt(String email, OtpPurpose purpose, boolean successful) {
        OtpAttempt attempt = new OtpAttempt();
        attempt.setEmail(email);
        attempt.setPurpose(purpose);
        attempt.setSuccessful(successful);
        otpAttemptRepository.save(attempt);
    }

    public AuthResponse login(LoginRequest request, String ipAddress) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);

        long emailFailures = loginAttemptRepository
                .countByEmailAndSuccessfulFalseAndAttemptedAtAfter(request.email(), windowStart);
        long ipFailures = loginAttemptRepository
                .countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(ipAddress, windowStart);

        if (emailFailures >= MAX_EMAIL_FAILURES || ipFailures >= MAX_IP_FAILURES) {
            throw new IllegalArgumentException("Too many failed login attempts. Try again later.");
        }

        User user = userRepository.findByEmail(request.email()).orElse(null);

        boolean successful = user != null
                && user.getPasswordHash() != null
                && passwordEncoder.matches(request.password(), user.getPasswordHash());

        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(request.email());
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(successful);
        loginAttemptRepository.save(attempt);

        if (!successful) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    private String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String otp = generateOtp();
            emailService.sendOtpEmail(user.getEmail(), otp);

            PendingPasswordReset pending = new PendingPasswordReset(
                    user.getEmail(), otp, System.currentTimeMillis());
            pendingPasswordResetCache.put(user.getEmail(), pending);
        });
        // always returns silently — no response leak on whether email exists
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (anamolyDetectionService.isOtpVerificationBlocked(request.email(), OtpPurpose.SIGNUP)) {
            throw new IllegalArgumentException("Too many failed attempts. Please try again later.");
        }

        PendingPasswordReset pending = pendingPasswordResetCache.getIfPresent(request.email());
        if (pending == null) {
            logOtpAttempt(request.email(), OtpPurpose.PASSWORD_RESET, false);
            throw new IllegalArgumentException("No password reset requested for this email");
        }

        boolean expired = System.currentTimeMillis() - pending.getOtpGeneratedAt() > OTP_VALIDITY.toMillis();
        if (expired) {
            pendingPasswordResetCache.invalidate(request.email());
            logOtpAttempt(request.email(), OtpPurpose.PASSWORD_RESET, false);
            throw new IllegalArgumentException("OTP has expired, please request a new one");
        }

        if (!pending.getOtpCode().equals(request.otp())) {
            logOtpAttempt(request.email(), OtpPurpose.PASSWORD_RESET, false);
            throw new InvalidOtpException("Invalid OTP");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid request"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        pendingPasswordResetCache.invalidate(request.email());
        logOtpAttempt(request.email(), OtpPurpose.PASSWORD_RESET, true);
    }
}
