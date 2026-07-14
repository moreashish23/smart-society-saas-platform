package com.smartsociety.analytics.scheduler;

import com.smartsociety.analytics.repository.DailyComplaintStatsRepository;
import com.smartsociety.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsScheduler {

    private final AnalyticsService              analyticsService;
    private final DailyComplaintStatsRepository dailyStatsRepo;


    @Scheduled(cron = "0 5 0 * * *")
    public void refreshAllSocietyStats() {
        log.info("Analytics nightly refresh started");

        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(90);

        List<UUID> societyIds = dailyStatsRepo.findDistinctSocietyIds(from, to);

        if (societyIds.isEmpty()) {
            log.info("Analytics nightly refresh: no active societies found");
            return;
        }

        int success = 0;
        int failed  = 0;

        for (UUID societyId : societyIds) {
            try {
                analyticsService.refreshDailyStats(societyId);
                success++;
            } catch (Exception ex) {
                log.error("Failed to refresh stats for societyId={}: {}",
                        societyId, ex.getMessage());
                failed++;
            }
        }

        log.info("Analytics nightly refresh completed — success={}, failed={}, total={}",
                success, failed, societyIds.size());
    }
}