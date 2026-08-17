package com.lakshith.secureshare.repository;

import com.lakshith.secureshare.model.FileRecord;
import com.lakshith.secureshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    List<FileRecord> findByUploader(User uploader);

    List<FileRecord> findByUploaderOrderByUploadedAtDesc(User uploader);

    @Query("""
        SELECT f FROM FileRecord f
        WHERE (:search IS NULL OR LOWER(f.originalFileName) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:uploaderUsername IS NULL OR f.uploader.username = :uploaderUsername)
        ORDER BY f.uploadedAt DESC
        """)
    List<FileRecord> searchFiles(
            @Param("search") String search,
            @Param("uploaderUsername") String uploaderUsername
    );
}
