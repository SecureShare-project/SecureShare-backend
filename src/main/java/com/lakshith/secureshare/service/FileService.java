package com.lakshith.secureshare.service;

import com.lakshith.secureshare.model.FileRecord;
import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.model.User;
import com.lakshith.secureshare.repository.FileRecordRepository;
import com.lakshith.secureshare.repository.ShareLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRecordRepository fileRecordRepository;
    private final ShareLinkRepository shareLinkRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public FileService(FileRecordRepository fileRecordRepository, ShareLinkRepository shareLinkRepository) {
        this.fileRecordRepository = fileRecordRepository;
        this.shareLinkRepository = shareLinkRepository;
    }

    public FileRecord storeFile(MultipartFile file, User uploader) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String storedFileName = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(storedFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            FileRecord record = new FileRecord();
            record.setOriginalFileName(originalFileName);
            record.setStoredFileName(storedFileName);
            record.setFilePath(targetPath.toString());
            record.setFileSize(file.getSize());
            record.setContentType(file.getContentType());
            record.setUploader(uploader);
            record.setUploadedAt(LocalDateTime.now());

            return fileRecordRepository.save(record);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public FileRecord replaceFile(Long oldFileId, MultipartFile newFile, User owner) {
        FileRecord oldRecord = fileRecordRepository.findById(oldFileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (!oldRecord.getUploader().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("File not found"); // same not-found-style response as revoke
        }

        // Mark old file as superseded
        oldRecord.setReplaced(true);
        fileRecordRepository.save(oldRecord);

        // Revoke every ShareLink pointing to the old file
        List<ShareLink> affectedLinks = shareLinkRepository.findByFileRecord(oldRecord);
        for (ShareLink link : affectedLinks) {
            link.setRevoked(true);
        }
        shareLinkRepository.saveAll(affectedLinks);

        // Store the new file as a normal upload
        return storeFile(newFile, owner);
    }

    public Path resolveFilePath(FileRecord fileRecord) {
        return Paths.get(fileRecord.getFilePath());
    }
}
