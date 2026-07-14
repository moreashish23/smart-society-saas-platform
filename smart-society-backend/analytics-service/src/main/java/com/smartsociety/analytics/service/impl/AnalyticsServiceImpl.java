package com.smartsociety.analytics.service.impl;

import com.smartsociety.analytics.client.ComplaintAnalyticsClient;
import com.smartsociety.analytics.client.VendorAnalyticsClient;
import com.smartsociety.analytics.dto.response.*;
import com.smartsociety.analytics.entity.DailyComplaintStats;
import com.smartsociety.analytics.entity.VendorPerformanceStats;
import com.smartsociety.analytics.repository.DailyComplaintStatsRepository;
import com.smartsociety.analytics.repository.VendorPerformanceStatsRepository;
import com.smartsociety.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ComplaintAnalyticsClient        complaintClient;
    private final VendorAnalyticsClient           vendorClient;
    private final DailyComplaintStatsRepository   dailyStatsRepo;
    private final VendorPerformanceStatsRepository vendorStatsRepo;

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID societyId) {
        // Fetch live complaint summary from complaint-service
        ComplaintAnalyticsClient.ComplaintSummary summary =
                complaintClient.getComplaintSummary(societyId);

        // Fetch 30-day trend
        ComplaintAnalyticsClient.ComplaintTrend trend =
                complaintClient.getComplaintTrend(societyId, 30);

        // Fetch vendor performance
        List<VendorAnalyticsClient.VendorPerformance> vendors =
                vendorClient.getVendorPerformance(societyId);

        // Compute SLA compliance rate
        double slaCompliance = summary.getTotal() > 0
                ? ((double)(summary.getTotal() - summary.getSlaBreaches()) / summary.getTotal()) * 100.0
                : 100.0;

        // Compute resident satisfaction (% closed without reopen)
        double satisfaction = summary.getClosed() > 0
                ? ((double)(summary.getClosed() - summary.getReopened()) / summary.getClosed()) * 100.0
                : 100.0;

        // Build top vendor stats
        List<DashboardResponse.VendorStat> topVendors = vendors.stream()
                .sorted((a, b) -> b.getRating().compareTo(a.getRating()))
                .limit(5)
                .map(v -> DashboardResponse.VendorStat.builder()
                        .vendorId(v.getVendorId())
                        .businessName(v.getBusinessName())
                        .serviceCategory(v.getServiceCategory())
                        .totalJobs(v.getTotalJobs())
                        .completedJobs(v.getCompletedJobs())
                        .rating(v.getRating())
                        .completionRate(v.getTotalJobs() > 0
                                ? (double) v.getCompletedJobs() / v.getTotalJobs() * 100.0
                                : 0.0)
                        .build())
                .collect(Collectors.toList());

        // Build trend points
        List<DashboardResponse.TrendPoint> trendPoints = trend.getDaily() != null
                ? trend.getDaily().stream()
                .map(d -> DashboardResponse.TrendPoint.builder()
                        .date(d.getDate()).created(d.getCreated())
                        .closed(d.getClosed()).escalated(d.getEscalated())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return DashboardResponse.builder()
                .societyId(societyId)
                .totalComplaints(summary.getTotal())
                .openComplaints(summary.getOpen())
                .closedComplaints(summary.getClosed())
                .inProgressComplaints(summary.getInProgress())
                .pendingVerificationComplaints(summary.getPendingVerification())
                .reopenedComplaints(summary.getReopened())
                .criticalComplaints(summary.getCritical())
                .slaBreaches(summary.getSlaBreaches())
                .avgResolutionHours(summary.getAvgResolutionHours())
                .criticalCount(summary.getCritical())
                .highCount(summary.getHigh())
                .mediumCount(summary.getMedium())
                .lowCount(summary.getLow())
                .slaComplianceRate(round(slaCompliance))
                .residentSatisfactionRate(round(satisfaction))
                .topVendors(topVendors)
                .complaintTrend(trendPoints)
                .build();
    }

    // ── COMPLAINT STATS (date range) ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ComplaintStatsResponse getComplaintStats(UUID societyId, LocalDate from, LocalDate to) {
        List<DailyComplaintStats> daily = dailyStatsRepo
                .findBySocietyIdAndStatDateBetweenOrderByStatDateAsc(societyId, from, to);

        long totalComplaints = daily.stream().mapToLong(DailyComplaintStats::getTotalComplaints).sum();
        long openComplaints  = daily.stream().mapToLong(DailyComplaintStats::getOpenComplaints).sum();
        long closedComplaints = daily.stream().mapToLong(DailyComplaintStats::getClosedComplaints).sum();
        long slaBreaches     = daily.stream().mapToLong(DailyComplaintStats::getSlaBreaches).sum();

        double avgResolution = daily.stream()
                .filter(d -> d.getAvgResolutionHours() != null)
                .mapToDouble(d -> d.getAvgResolutionHours().doubleValue())
                .average().orElse(0.0);

        double slaCompliance = totalComplaints > 0
                ? ((double)(totalComplaints - slaBreaches) / totalComplaints) * 100.0
                : 100.0;

        List<DailyStatResponse> dailyStats = daily.stream()
                .map(d -> DailyStatResponse.builder()
                        .date(d.getStatDate())
                        .totalComplaints(d.getTotalComplaints())
                        .openComplaints(d.getOpenComplaints())
                        .closedComplaints(d.getClosedComplaints())
                        .slaBreaches(d.getSlaBreaches())
                        .avgResolutionHours(d.getAvgResolutionHours())
                        .build())
                .collect(Collectors.toList());

        return ComplaintStatsResponse.builder()
                .societyId(societyId).from(from).to(to)
                .totalComplaints(totalComplaints)
                .openComplaints(openComplaints)
                .closedComplaints(closedComplaints)
                .slaBreaches(slaBreaches)
                .avgResolutionHours(round(avgResolution))
                .slaComplianceRate(round(slaCompliance))
                .dailyStats(dailyStats)
                .build();
    }

    // ── VENDOR PERFORMANCE ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<VendorPerformanceResponse> getVendorPerformance(UUID societyId, LocalDate month) {
        LocalDate statMonth = month.withDayOfMonth(1);
        return vendorStatsRepo.findTopVendorsByMonth(societyId, statMonth)
                .stream()
                .map(v -> VendorPerformanceResponse.builder()
                        .vendorId(v.getVendorId())
                        .businessName(v.getVendorName())
                        .statMonth(v.getStatMonth())
                        .jobsAssigned(v.getJobsAssigned())
                        .jobsCompleted(v.getJobsCompleted())
                        .jobsCancelled(v.getJobsCancelled())
                        .avgRating(
                                v.getAvgRating() != null
                                        ? v.getAvgRating().doubleValue()
                                        : 0.0
                        )
                        .completionRate(v.getJobsAssigned() > 0
                                ? round((double) v.getJobsCompleted() / v.getJobsAssigned() * 100.0)
                                : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    // ── TREND ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DashboardResponse.TrendPoint> getComplaintTrend(UUID societyId, int days) {
        ComplaintAnalyticsClient.ComplaintTrend trend =
                complaintClient.getComplaintTrend(societyId, days);
        if (trend.getDaily() == null) return List.of();
        return trend.getDaily().stream()
                .map(d -> DashboardResponse.TrendPoint.builder()
                        .date(d.getDate()).created(d.getCreated())
                        .closed(d.getClosed()).escalated(d.getEscalated())
                        .build())
                .collect(Collectors.toList());
    }

    // ── REFRESH DAILY STATS (called by scheduler) ─────────────────────────────

    @Override
    @Transactional
    public void refreshDailyStats(UUID societyId) {
        ComplaintAnalyticsClient.ComplaintSummary summary =
                complaintClient.getComplaintSummary(societyId);

        LocalDate today = LocalDate.now();

        DailyComplaintStats stats = dailyStatsRepo
                .findBySocietyIdAndStatDate(societyId, today)
                .orElse(DailyComplaintStats.builder()
                        .societyId(societyId).statDate(today).build());

        stats.setTotalComplaints((int) summary.getTotal());
        stats.setOpenComplaints((int) summary.getOpen());
        stats.setClosedComplaints((int) summary.getClosed());
        stats.setReopenedComplaints((int) summary.getReopened());
        stats.setCriticalComplaints((int) summary.getCritical());
        stats.setHighComplaints((int) summary.getHigh());
        stats.setMediumComplaints((int) summary.getMedium());
        stats.setLowComplaints((int) summary.getLow());
        stats.setSlaBreaches((int) summary.getSlaBreaches());
        stats.setAvgResolutionHours(BigDecimal.valueOf(summary.getAvgResolutionHours())
                .setScale(2, RoundingMode.HALF_UP));

        dailyStatsRepo.save(stats);
        log.info("Daily stats refreshed for societyId={}, date={}", societyId, today);
    }

    //  helpers

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}