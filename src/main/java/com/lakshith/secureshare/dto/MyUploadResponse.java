package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.FileRecord;
import java.time.LocalDateTime;

public record MyUploadResponse(
        Long id,
        String originalFileName,
        Long fileSize,
        String contentType,
        LocalDateTime uploadedAt
) {
    public static MyUploadResponse from(FileRecord record) {
        return new MyUploadResponse(
                record.getId(),
                record.getOriginalFileName(),
                record.getFileSize(),
                record.getContentType(),
                record.getUploadedAt()
        );
    }
}