package com.lakshith.secureshare.repository;

import com.lakshith.secureshare.model.FileRecord;
import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByToken(String token);
    List<ShareLink> findByOwner(User owner);
    List<ShareLink> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<ShareLink> findByIdAndOwner(Long id, User owner);
    List<ShareLink> findByFileRecord(FileRecord fileRecord);

    @Query("""
    SELECT DISTINCT sl FROM ShareLink sl
    JOIN AccessLog al ON al.shareToken = sl.token
    WHERE sl.expiresAt < :now
    AND al.successful = true
    """)
    List<ShareLink> findExpiredAndDownloadedShares(@Param("now") LocalDateTime now);
}
