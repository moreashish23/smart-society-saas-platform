package com.smartsociety.analytics.client;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@FeignClient(
        name     = "vendor-analytics-client",
        url      = "${app.services.vendor-service-url}",
        fallback = VendorAnalyticsClient.Fallback.class
)
public interface VendorAnalyticsClient {

    @GetMapping("/api/vendors/analytics/performance")
    List<VendorPerformance> getVendorPerformance(@RequestParam("societyId") UUID societyId);

    // ── Response model — must match VendorAnalyticsController.VendorPerformanceResponse ──

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class VendorPerformance {
        private UUID       vendorId;
        private String     businessName;
        private String     serviceCategory;
        private Integer    totalJobs;
        private Integer    completedJobs;
        private BigDecimal rating;
    }

    // ── Fallback ──────────────────────────────────────────────────────────────

    @lombok.extern.slf4j.Slf4j
    @org.springframework.stereotype.Component
    class Fallback implements VendorAnalyticsClient {

        @Override
        public List<VendorPerformance> getVendorPerformance(UUID societyId) {
            log.warn("vendor-service unavailable — returning empty vendor performance for societyId={}", societyId);
            return List.of();
        }
    }
}