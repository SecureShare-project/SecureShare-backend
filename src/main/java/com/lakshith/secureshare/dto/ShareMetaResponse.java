package com.lakshith.secureshare.dto;

import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.model.ShareType;
import com.lakshith.secureshare.model.AccessType;

// Metadata-only response for the public share landing page — deliberately excludes
// textContent and any file bytes/download URL, since access hasn't been granted yet.
public record ShareMetaResponse(
        String token,
        ShareType type,
        AccessType accessType,
        String itemName,      // originalFileName for FILE shares, or a generic label for TEXT shares
        Long sizeBytes,       // null for TEXT shares
        boolean expired
) {
    public static ShareMetaResponse from(ShareLink link) {
        String itemName = link.getType() == ShareType.FILE
                ? link.getFileRecord().getOriginalFileName()
                : "Shared Text";

        Long sizeBytes = link.getType() == ShareType.FILE
                ? link.getFileRecord().getFileSize()
                : null;

        boolean expired = link.getExpiresAt() != null
                && link.getExpiresAt().isBefore(java.time.LocalDateTime.now());

        return new ShareMetaResponse(
                link.getToken(),
                link.getType(),
                link.getAccessType(),
                itemName,
                sizeBytes,
                expired || link.isRevoked()
        );
    }
}