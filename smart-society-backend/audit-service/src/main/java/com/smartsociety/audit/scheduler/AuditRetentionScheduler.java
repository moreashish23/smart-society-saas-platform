package com.smartsociety.audit.scheduler;

import com.smartsociety.audit.config.AppProperties;
import com.smartsociety.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditRetentionScheduler {

    private final AuditService   auditService;
    private final AppProperties  appProperties;


    @Scheduled(cron = "0 0 3 * * SUN")
    public void runRetentionPurge() {
        int retentionDays = appProperties.getRetentionDays();
        log.info("Audit retention purge started — removing logs older than {} days", retentionDays);
        try {
            int deleted = auditService.purgeOldLogs(retentionDays);
            log.info("Audit retention purge completed — {} records removed", deleted);
        } catch (Exception ex) {
            log.error("Audit retention purge failed: {}", ex.getMessage(), ex);
        }
    }
}