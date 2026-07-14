package com.smartsociety.analytics.client;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;


@FeignClient(
        name     = "complaint-analytics-client",
        url      = "${app.services.complaint-service-url}",
        fallback = ComplaintAnalyticsClient.Fallback.class
)
public interface ComplaintAnalyticsClient {

    @GetMapping("/api/complaints/analytics/summary")
    ComplaintSummary getComplaintSummary(@RequestParam("societyId") UUID societyId);

    @GetMapping("/api/complaints/analytics/trend")
    ComplaintTrend getComplaintTrend(
            @RequestParam("societyId") UUID societyId,
            @RequestParam("days")      int  days);

    // ── Response models — must mirror ComplaintAnalyticsController exactly ─────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class ComplaintSummary {
        private long   total;
        private long   open;
        private long   assigned;           // Fix 3: was missing
        private long   inProgress;
        private long   pendingVerification;
        private long   closed;
        private long   cancelled;          // Fix 3: was missing
        private long   reopened;
        private long   critical;
        private long   high;
        private long   medium;
        private long   low;
        private long   slaBreaches;
        private double avgResolutionHours;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class ComplaintTrend {
        private List<DailyCount> daily;

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class DailyCount {
            private String date;
            private long   created;
            private long   closed;
            private long   escalated;
        }
    }



    @lombok.extern.slf4j.Slf4j
    @org.springframework.stereotype.Component
    class Fallback implements ComplaintAnalyticsClient {

        @Override
        public ComplaintSummary getComplaintSummary(UUID societyId) {
            log.warn("complaint-service unavailable — returning empty summary for societyId={}",
                    societyId);
            return ComplaintSummary.builder()
                    .total(0).open(0).assigned(0).inProgress(0)
                    .pendingVerification(0).closed(0).cancelled(0).reopened(0)
                    .critical(0).high(0).medium(0).low(0)
                    .slaBreaches(0).avgResolutionHours(0.0)
                    .build();
        }

        @Override
        public ComplaintTrend getComplaintTrend(UUID societyId, int days) {
            log.warn("complaint-service unavailable — returning empty trend for societyId={}",
                    societyId);
            return ComplaintTrend.builder().daily(List.of()).build();
        }
    }
}
