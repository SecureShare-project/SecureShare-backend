package com.lakshith.secureshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shareToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareType itemType;

    @Column(nullable = true)
    private String itemName;

    @Column(nullable = false)
    private String ownerUsername;

    @Column(nullable = true)
    private String accessedByEmail;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private boolean successful;

    @Column(updatable = false)
    private LocalDateTime accessedAt = LocalDateTime.now();
}