package com.lakshith.secureshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String storedFileName; // randomized name on disk to avoid collisions/overwrites

    @Column(nullable = false)
    private String filePath; // full path on disk where the file lives

    @Column(nullable = false)
    private Long fileSize; // bytes

    @Column(nullable = false)
    private String contentType; // MIME type, e.g. application/pdf

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean replaced = false;
}