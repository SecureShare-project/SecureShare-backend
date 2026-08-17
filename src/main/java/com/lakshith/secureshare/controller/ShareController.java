package com.lakshith.secureshare.controller;

import com.lakshith.secureshare.dto.*;
import com.lakshith.secureshare.model.*;
import com.lakshith.secureshare.repository.FileRecordRepository;
import com.lakshith.secureshare.repository.UserRepository;
import com.lakshith.secureshare.service.ShareAccessService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.lakshith.secureshare.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareAccessService shareAccessService;
    private final UserRepository userRepository;
    private final FileRecordRepository fileRecordRepository;
    private final FileService fileService;

    public ShareController(
            ShareAccessService shareAccessService,
            UserRepository userRepository,
            FileRecordRepository fileRecordRepository,
            FileService fileService) {
        this.shareAccessService = shareAccessService;
        this.userRepository = userRepository;
        this.fileRecordRepository = fileRecordRepository;
        this.fileService = fileService;
    }

    @PostMapping("/text")
    public ResponseEntity<ShareLinkResponse> createTextShare(
            @RequestBody CreateTextShareRequest request,
            Authentication authentication) {

        User owner = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        ShareLink link = shareAccessService.createTextShare(
                request.textContent(),
                owner,
                request.accessType(),
                request.password(),
                request.expiryMinutes()
        );

        return ResponseEntity.ok(ShareLinkResponse.from(link));
    }

    @PostMapping("/file/{fileRecordId}")
    public ResponseEntity<ShareLinkResponse> createFileShare(
            @PathVariable Long fileRecordId,
            @RequestBody CreateFileShareRequest request,
            Authentication authentication) {

        User owner = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        FileRecord fileRecord = fileRecordRepository.findById(fileRecordId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        ShareLink link = shareAccessService.createFileShare(
                fileRecord,
                owner,
                request.accessType(),
                request.password(),
                request.expiryMinutes()
        );

        return ResponseEntity.ok(ShareLinkResponse.from(link));
    }

    @PostMapping("/{token}/access")
    public ResponseEntity<ShareLinkResponse> accessShare(
            @PathVariable String token,
            @RequestBody(required = false) AccessShareRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String password = request != null ? request.password() : null;
        String authenticatedEmail = authentication != null ? authentication.getName() : null;
        String ipAddress = httpRequest.getRemoteAddr();

        ShareLink link = shareAccessService.resolveShare(token, password, authenticatedEmail, ipAddress);
        return ResponseEntity.ok(ShareLinkResponse.from(link));
    }

    @PostMapping("/{token}/download")
    public ResponseEntity<Resource> downloadSharedFile(
            @PathVariable String token,
            @RequestBody(required = false) AccessShareRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String password = request != null ? request.password() : null;
        String authenticatedEmail = authentication != null ? authentication.getName() : null;
        String ipAddress = httpRequest.getRemoteAddr();

        ShareLink link = shareAccessService.resolveShare(token, password, authenticatedEmail, ipAddress);

        if (link.getType() != ShareType.FILE || link.getFileRecord() == null) {
            throw new IllegalArgumentException("This share does not contain a file");
        }

        FileRecord fileRecord = link.getFileRecord();
        Path filePath = fileService.resolveFilePath(fileRecord);

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File no longer exists on disk");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileRecord.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileRecord.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{token}")
    public ResponseEntity<ShareMetaResponse> getShareMeta(@PathVariable String token) {
        ShareLink link = shareAccessService.getShareMetaOnly(token);
        return ResponseEntity.ok(ShareMetaResponse.from(link));
    }
}