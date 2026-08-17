package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.AccessType;
import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.model.ShareType;

import java.time.LocalDateTime;

public record ShareLinkResponse(
        String token,
        ShareType type,
        String textContent,
        String fileName,
        AccessType accessType,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static ShareLinkResponse from(ShareLink link) {
        return new ShareLinkResponse(
                link.getToken(),
                link.getType(),
                link.getTextContent(),
                link.getFileRecord() != null ? link.getFileRecord().getOriginalFileName() : null,
                link.getAccessType(),
                link.getExpiresAt(),
                link.getCreatedAt()
        );
    }
}
