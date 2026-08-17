package com.lakshith.secureshare.service;

import com.lakshith.secureshare.model.*;
import com.lakshith.secureshare.repository.AccessLogRepository;
import com.lakshith.secureshare.repository.ShareLinkRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareAccessService {

    private final ShareLinkRepository shareLinkRepository;
    private final AccessLogRepository accessLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnamolyDetectionService anamolyDetectionService;

    public ShareAccessService(
            ShareLinkRepository shareLinkRepository,
            AccessLogRepository accessLogRepository,
            PasswordEncoder passwordEncoder,
            AnamolyDetectionService anamolyDetectionService) {
        this.shareLinkRepository = shareLinkRepository;
        this.accessLogRepository = accessLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.anamolyDetectionService = anamolyDetectionService;
    }

    public ShareLink createTextShare(String textContent, User owner, AccessType accessType,
                                     String rawPassword, long expiryMinutes) {
        ShareLink link = buildBaseShareLink(owner, accessType, rawPassword, expiryMinutes);
        link.setType(ShareType.TEXT);
        link.setTextContent(textContent);
        return shareLinkRepository.save(link);
    }

    public ShareLink createFileShare(FileRecord fileRecord, User owner, AccessType accessType,
                                     String rawPassword, long expiryMinutes) {
        ShareLink link = buildBaseShareLink(owner, accessType, rawPassword, expiryMinutes);
        link.setType(ShareType.FILE);
        link.setFileRecord(fileRecord);
        return shareLinkRepository.save(link);
    }

    private ShareLink buildBaseShareLink(User owner, AccessType accessType,
                                         String rawPassword, long expiryMinutes) {
        ShareLink link = new ShareLink();
        link.setToken(UUID.randomUUID().toString());
        link.setOwner(owner);
        link.setAccessType(accessType);
        link.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));

        if (accessType == AccessType.PASSWORD) {
            if (rawPassword == null || rawPassword.isBlank()) {
                throw new IllegalArgumentException("Password required for password-protected shares");
            }
            link.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        return link;
    }

    public ShareLink resolveShare(String token, String suppliedPassword, String authenticatedEmail, String ipAddress) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Share link not found"));

        if (anamolyDetectionService.isShareAccessBlocked(link.getToken())) {
            throw new IllegalArgumentException("Too many failed attempts on this share. Please try again later.");
        }

        if (link.getExpiresAt().isBefore(LocalDateTime.now())) {
            logAttempt(link, authenticatedEmail, ipAddress, false);
            throw new IllegalArgumentException("This share link has expired");
        }

        boolean authorized = switch (link.getAccessType()) {
            case PASSWORD -> suppliedPassword != null &&
                    passwordEncoder.matches(suppliedPassword, link.getPasswordHash());
            case AUTHENTICATED_USER -> authenticatedEmail != null;
        };

        logAttempt(link, authenticatedEmail, ipAddress, authorized);

        if (!authorized) {
            throw new IllegalArgumentException("Access denied");
        }

        return link;
    }

    private void logAttempt(ShareLink link, String accessedByEmail, String ipAddress, boolean successful) {
        AccessLog log = new AccessLog();
        log.setShareToken(link.getToken());
        log.setItemType(link.getType());
        log.setItemName(link.getType() == ShareType.FILE && link.getFileRecord() != null
                ? link.getFileRecord().getOriginalFileName()
                : null);
        log.setOwnerUsername(link.getOwner().getUsername());
        log.setAccessedByEmail(accessedByEmail);
        log.setIpAddress(ipAddress);
        log.setSuccessful(successful);
        accessLogRepository.save(log);
    }

    // Returns share metadata WITHOUT validating password or authentication —
// safe to call before the user has unlocked anything.
    public ShareLink getShareMetaOnly(String token) {
        return shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Share not found"));
    }
}
