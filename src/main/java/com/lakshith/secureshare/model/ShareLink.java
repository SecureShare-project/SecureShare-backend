package com.lakshith.secureshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "share_links")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // random string used in the public URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareType type; // FILE or TEXT

    @Lob
    @Column(nullable = true) // only set when type == TEXT
    private String textContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_record_id", nullable = true) // only set when type == FILE
    private FileRecord fileRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType; // PASSWORD or AUTHENTICATED_USER

    @Column(nullable = true) // only set when accessType == PASSWORD
    private String passwordHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean revoked = false;
}
