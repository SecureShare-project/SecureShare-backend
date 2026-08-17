package com.lakshith.secureshare.service;

import com.lakshith.secureshare.model.ShareLink;
import com.lakshith.secureshare.repository.ShareLinkRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShareCleanupService {

    private final ShareLinkRepository shareLinkRepository;

    public ShareCleanupService(ShareLinkRepository shareLinkRepository) {
        this.shareLinkRepository = shareLinkRepository;
    }

    @Scheduled(cron = "*/10 * * * * *") // runs every hour, on the hour
    public void deleteExpiredAndDownloadedShares() {
        List<ShareLink> toDelete = shareLinkRepository.findExpiredAndDownloadedShares(LocalDateTime.now());

        if (!toDelete.isEmpty()) {
            shareLinkRepository.deleteAll(toDelete);
        }
    }
}