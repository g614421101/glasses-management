package com.glasses.scheduler;

import com.glasses.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 操作日志定时清理：每天凌晨按配置的保留天数自动清理过期日志
 * （app.operation-log.retention-days，0 表示不自动清理）。
 */
@Slf4j
@Component
public class OperationLogCleanupScheduler {

    @Autowired
    private OperationLogService operationLogService;

    /** 操作日志保留天数（application.yml 配置，0 表示不自动清理） */
    @Value("${app.operation-log.retention-days:90}")
    private int retentionDays;

    @Scheduled(cron = "0 25 3 * * ?")
    public void cleanupExpiredLogs() {
        if (retentionDays <= 0) {
            log.info("操作日志自动清理未启用（retention-days={}）", retentionDays);
            return;
        }
        try {
            int count = operationLogService.cleanupBefore(retentionDays);
            log.info("定时清理操作日志: 保留{}天, 清理{}条", retentionDays, count);
        } catch (Exception e) {
            log.error("定时清理操作日志失败", e);
        }
    }
}
