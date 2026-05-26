package com.glasses.config;

import com.glasses.service.RecycleBinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecycleCleanupScheduler {

    @Autowired
    private RecycleBinService recycleBinService;

    @Scheduled(cron = "0 20 3 * * ?")
    public void purgeExpiredRecycleBinItems() {
        try {
            var result = recycleBinService.purgeExpired();
            log.info("定时清理过期回收站: {}", result);
        } catch (Exception e) {
            log.error("定时清理过期回收站失败", e);
        }
    }
}
