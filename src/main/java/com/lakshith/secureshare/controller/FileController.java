package com.lakshith.secureshare.controller;

import com.lakshith.secureshare.dto.FileRecordResponse;
import com.lakshith.secureshare.model.FileRecord;
import com.lakshith.secureshare.model.User;
import com.lakshith.secureshare.repository.FileRecordRepository;
import com.lakshith.secureshare.repository.UserRepository;
import com.lakshith.secureshare.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final UserRepository userRepository;
    private final FileRecordRepository fileRecordRepository;

    public FileController(
            FileService fileService,
            UserRepository userRepository,
            FileRecordRepository fileRecordRepository) {
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.fileRecordRepository = fileRecordRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileRecordResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User uploader = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        FileRecord record = fileService.storeFile(file, uploader);
        return ResponseEntity.ok(FileRecordResponse.from(record));
    }

    // GLOBAL SEARCH: all files from all authors
    @GetMapping
    public ResponseEntity<List<FileRecordResponse>> listFiles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String uploader) {

        List<FileRecord> records = fileRecordRepository.searchFiles(search, uploader);
        List<FileRecordResponse> response = records.stream()
                .map(FileRecordResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/replace")
    public ResponseEntity<FileRecordResponse> replaceFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User owner = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        FileRecord newRecord = fileService.replaceFile(id, file, owner);
        return ResponseEntity.ok(FileRecordResponse.from(newRecord));
    }
}