package com.lakshith.secureshare.controller;

import com.lakshith.secureshare.dto.AccessLogResponse;
import com.lakshith.secureshare.dto.MyShareResponse;
import com.lakshith.secureshare.dto.MyUploadResponse;
import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.model.User;
import com.lakshith.secureshare.repository.AccessLogRepository;
import com.lakshith.secureshare.repository.FileRecordRepository;
import com.lakshith.secureshare.repository.ShareLinkRepository;
import com.lakshith.secureshare.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AccessLogRepository accessLogRepository;
    private final FileRecordRepository fileRecordRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final UserRepository userRepository;

    public DashboardController(
            AccessLogRepository accessLogRepository,
            FileRecordRepository fileRecordRepository,
            ShareLinkRepository shareLinkRepository,
            UserRepository userRepository) {
        this.accessLogRepository = accessLogRepository;
        this.fileRecordRepository = fileRecordRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.userRepository = userRepository;
    }

    // Things this user has successfully accessed from others' shares
    @GetMapping("/accessed")
    public ResponseEntity<List<AccessLogResponse>> myAccessedItems(
            @RequestParam(required = false) String search,
            Authentication authentication) {

        List<AccessLogResponse> response = accessLogRepository
                .searchMyAccessedItems(authentication.getName(), search)
                .stream()
                .map(AccessLogResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    // Files this user has uploaded
    @GetMapping("/uploads")
    public ResponseEntity<List<MyUploadResponse>> myUploads(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        List<MyUploadResponse> response = fileRecordRepository
                .findByUploaderOrderByUploadedAtDesc(user)
                .stream()
                .map(MyUploadResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    // Shares this user has created
    @GetMapping("/shares")
    public ResponseEntity<List<MyShareResponse>> myShares(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        List<MyShareResponse> response = shareLinkRepository
                .findByOwnerOrderByCreatedAtDesc(user)
                .stream()
                .map(MyShareResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/shares/{id}/revoke")
    public ResponseEntity<Void> revokeShare(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        ShareLink link = shareLinkRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Share not found or not owned by you"));

        link.setRevoked(true);
        shareLinkRepository.save(link);

        return ResponseEntity.noContent().build();
    }
}