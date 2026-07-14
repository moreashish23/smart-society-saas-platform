package com.smartsociety.analytics.controller;

import com.smartsociety.analytics.dto.response.*;
import com.smartsociety.analytics.exception.AccessDeniedException;
import com.smartsociety.analytics.security.RequestContext;
import com.smartsociety.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Society dashboard, complaint stats, vendor performance, and SLA reports")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RequestContext   requestContext;

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Get full analytics dashboard [Manager+]",
            description = "Returns complaint KPIs, trend data, top vendors, SLA compliance, and resident satisfaction rate for the authenticated user's society.")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        requireManagerOrAbove();
        DashboardResponse dashboard = analyticsService.getDashboard(requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/dashboard/{societyId}")
    @Operation(summary = "Get dashboard for a specific society [SUPER_ADMIN only]")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardForSociety(
            @PathVariable UUID societyId) {
        requireSuperAdmin();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboard(societyId)));
    }

    // ── COMPLAINT STATS ───────────────────────────────────────────────────────

    @GetMapping("/complaints")
    @Operation(summary = "Get complaint stats for a date range [Manager+]",
            description = "Returns daily breakdown with totals, open/closed counts, SLA breaches, and average resolution time.")
    public ResponseEntity<ApiResponse<ComplaintStatsResponse>> getComplaintStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        requireManagerOrAbove();

        LocalDate effectiveTo   = to   != null ? to   : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(29);

        ComplaintStatsResponse response = analyticsService.getComplaintStats(
                requestContext.getSocietyId(), effectiveFrom, effectiveTo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── COMPLAINT TREND ───────────────────────────────────────────────────────

    @GetMapping("/complaints/trend")
    @Operation(summary = "Get complaint creation/closure trend [Manager+]",
            description = "Returns daily created, closed, and escalated counts for charting.")
    public ResponseEntity<ApiResponse<List<DashboardResponse.TrendPoint>>> getComplaintTrend(
            @RequestParam(defaultValue = "30") int days) {

        requireManagerOrAbove();
        if (days < 1 || days > 365) days = 30;

        List<DashboardResponse.TrendPoint> trend =
                analyticsService.getComplaintTrend(requestContext.getSocietyId(), days);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    // ── VENDOR PERFORMANCE ────────────────────────────────────────────────────

    @GetMapping("/vendors/performance")
    @Operation(summary = "Get vendor performance stats for a month [Manager+]",
            description = "Returns jobs assigned/completed/cancelled and average rating per vendor for the given month.")
    public ResponseEntity<ApiResponse<List<VendorPerformanceResponse>>> getVendorPerformance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {

        requireManagerOrAbove();
        LocalDate effectiveMonth = month != null ? month : LocalDate.now();

        List<VendorPerformanceResponse> performance =
                analyticsService.getVendorPerformance(requestContext.getSocietyId(), effectiveMonth);
        return ResponseEntity.ok(ApiResponse.success(performance));
    }

    // ── MANUAL REFRESH (Super Admin) ──────────────────────────────────────────

    @PostMapping("/refresh")
    @Operation(summary = "Manually trigger analytics data refresh [SUPER_ADMIN only]")
    public ResponseEntity<ApiResponse<Void>> refreshStats() {
        requireSuperAdmin();
        analyticsService.refreshDailyStats(requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(null, "Analytics data refreshed successfully"));
    }

    // ── Guards ────────────────────────────────────────────────────────────────

    private void requireManagerOrAbove() {
        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();
    }

    private void requireSuperAdmin() {
        if (!requestContext.isSuperAdmin()) throw new AccessDeniedException();
    }
}