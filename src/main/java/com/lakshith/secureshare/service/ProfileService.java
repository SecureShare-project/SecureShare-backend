package com.lakshith.secureshare.service;

import com.lakshith.secureshare.model.FileRecord;
import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.model.User;
import com.lakshith.secureshare.repository.FileRecordRepository;
import com.lakshith.secureshare.repository.ShareLinkRepository;
import com.lakshith.secureshare.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final FileRecordRepository fileRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    public ProfileService(
            UserRepository userRepository,
            ShareLinkRepository shareLinkRepository,
            FileRecordRepository fileRecordRepository,
            PasswordEncoder passwordEncoder,
            FileService fileService) {
        this.userRepository = userRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.fileRecordRepository = fileRecordRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void changeUsername(String email, String newUsername) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username already taken");
        }

        user.setUsername(newUsername);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete all share links owned by this user
        List<ShareLink> ownedShares = shareLinkRepository.findByOwner(user);
        shareLinkRepository.deleteAll(ownedShares);

        // Delete all files uploaded by this user (disk + record)
        List<FileRecord> ownedFiles = fileRecordRepository.findByUploader(user);
        for (FileRecord fileRecord : ownedFiles) {
            Path path = fileService.resolveFilePath(fileRecord);
            try {
                Files.deleteIfExists(path);
            } catch (Exception ignored) {
                // log in real system; proceed with DB cleanup regardless
            }
        }
        fileRecordRepository.deleteAll(ownedFiles);

        userRepository.delete(user);
    }
}
