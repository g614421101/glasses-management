package com.glasses.config;

import com.glasses.service.RecycleBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecycleCleanupScheduler {

    @Autowired
    private RecycleBinService recycleBinService;

    @Scheduled(cron = "0 20 3 * * ?")
    public void purgeExpiredRecycleBinItems() {
        recycleBinService.purgeExpired();
    }
}
