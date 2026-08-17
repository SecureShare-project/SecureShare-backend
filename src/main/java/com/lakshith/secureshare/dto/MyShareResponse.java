package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.model.ShareType;
import java.time.LocalDateTime;

public record MyShareResponse(
        Long id,
        String token,
        ShareType type,
        String itemName,
        String status, // ACTIVE, EXPIRED, REVOKED
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static MyShareResponse from(ShareLink link) {
        String status;
        if (link.isRevoked()) {
            status = "REVOKED";
        } else if (link.getExpiresAt().isBefore(LocalDateTime.now())) {
            status = "EXPIRED";
        } else {
            status = "ACTIVE";
        }

        String itemName = link.getType() == ShareType.FILE && link.getFileRecord() != null
                ? link.getFileRecord().getOriginalFileName()
                : "Text share";

        return new MyShareResponse(
                link.getId(),
                link.getToken(),
                link.getType(),
                itemName,
                status,
                link.getExpiresAt(),
                link.getCreatedAt()
        );
    }
}