package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.FileRecord;

import java.time.LocalDateTime;

public record FileRecordResponse(
        Long id,
        String originalFileName,
        Long fileSize,
        String contentType,
        String uploadedByUsername,
        LocalDateTime uploadedAt
) {
    public static FileRecordResponse from(FileRecord record) {
        return new FileRecordResponse(
                record.getId(),
                record.getOriginalFileName(),
                record.getFileSize(),
                record.getContentType(),
                record.getUploader().getUsername(),
                record.getUploadedAt()
        );
    }
}