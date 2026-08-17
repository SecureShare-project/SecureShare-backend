package com.lakshith.secureshare.repository;

import com.lakshith.secureshare.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    long countByShareTokenAndSuccessfulFalseAndAccessedAtAfter(String shareToken, LocalDateTime after);

    List<AccessLog> findByAccessedByEmailOrderByAccessedAtDesc(String email);

    @Query("""
        SELECT a FROM AccessLog a
        WHERE a.accessedByEmail = :email
        AND a.successful = true
        AND (:search IS NULL OR LOWER(a.itemName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY a.accessedAt DESC
        """)
    List<AccessLog> searchMyAccessedItems(
            @Param("email") String email,
            @Param("search") String search
    );
}