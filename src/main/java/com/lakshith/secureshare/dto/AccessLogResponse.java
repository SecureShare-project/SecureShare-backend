package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.AccessLog;
import com.lakshith.secureshare.model.ShareType;

import java.time.LocalDateTime;

public record AccessLogResponse(
        String shareToken,
        ShareType itemType,
        String itemName,
        String ownerUsername,
        LocalDateTime accessedAt
) {
    public static AccessLogResponse from(AccessLog log) {
        return new AccessLogResponse(
                log.getShareToken(),
                log.getItemType(),
                log.getItemName(),
                log.getOwnerUsername(),
                log.getAccessedAt()
        );
    }
}
