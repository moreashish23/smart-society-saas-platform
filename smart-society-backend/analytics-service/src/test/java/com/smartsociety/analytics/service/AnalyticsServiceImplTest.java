package com.smartsociety.analytics.service;

import com.smartsociety.analytics.client.ComplaintAnalyticsClient;
import com.smartsociety.analytics.client.VendorAnalyticsClient;
import com.smartsociety.analytics.dto.response.DashboardResponse;
import com.smartsociety.analytics.entity.DailyComplaintStats;
import com.smartsociety.analytics.repository.DailyComplaintStatsRepository;
import com.smartsociety.analytics.repository.VendorPerformanceStatsRepository;
import com.smartsociety.analytics.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsServiceImpl Unit Tests")
class AnalyticsServiceImplTest {

    @Mock private ComplaintAnalyticsClient         complaintClient;
    @Mock private VendorAnalyticsClient            vendorClient;
    @Mock private DailyComplaintStatsRepository    dailyStatsRepo;
    @Mock private VendorPerformanceStatsRepository vendorStatsRepo;

    @InjectMocks private AnalyticsServiceImpl analyticsService;

    private UUID societyId;

    @BeforeEach
    void setUp() {
        societyId = UUID.randomUUID();
    }

    @Nested @DisplayName("getDashboard()")
    class DashboardTests {

        @Test @DisplayName("Should build dashboard with correct KPIs")
        void getDashboard_computesKpisCorrectly() {
            ComplaintAnalyticsClient.ComplaintSummary summary =
                    ComplaintAnalyticsClient.ComplaintSummary.builder()
                            .total(100).open(20).closed(70).inProgress(5)
                            .pendingVerification(5).reopened(8)
                            .critical(3).high(15).medium(40).low(42)
                            .slaBreaches(10).avgResolutionHours(18.5)
                            .build();

            ComplaintAnalyticsClient.ComplaintTrend trend =
                    ComplaintAnalyticsClient.ComplaintTrend.builder().daily(List.of()).build();

            when(complaintClient.getComplaintSummary(societyId)).thenReturn(summary);
            when(complaintClient.getComplaintTrend(eq(societyId), anyInt())).thenReturn(trend);
            when(vendorClient.getVendorPerformance(societyId)).thenReturn(List.of());

            DashboardResponse dashboard = analyticsService.getDashboard(societyId);

            assertThat(dashboard.getTotalComplaints()).isEqualTo(100);
            assertThat(dashboard.getOpenComplaints()).isEqualTo(20);
            assertThat(dashboard.getClosedComplaints()).isEqualTo(70);
            assertThat(dashboard.getSlaBreaches()).isEqualTo(10);
            // SLA compliance = (100-10)/100 * 100 = 90.0%
            assertThat(dashboard.getSlaComplianceRate()).isEqualTo(90.0);
            // Satisfaction = (70-8)/70 * 100 = 88.57%
            assertThat(dashboard.getResidentSatisfactionRate()).isEqualTo(88.57);
        }

        @Test @DisplayName("Should return 100% compliance when no complaints exist")
        void getDashboard_noComplaints_100PercentCompliance() {
            ComplaintAnalyticsClient.ComplaintSummary summary =
                    new ComplaintAnalyticsClient.ComplaintSummary();
            ComplaintAnalyticsClient.ComplaintTrend trend =
                    ComplaintAnalyticsClient.ComplaintTrend.builder().daily(List.of()).build();

            when(complaintClient.getComplaintSummary(societyId)).thenReturn(summary);
            when(complaintClient.getComplaintTrend(eq(societyId), anyInt())).thenReturn(trend);
            when(vendorClient.getVendorPerformance(societyId)).thenReturn(List.of());

            DashboardResponse dashboard = analyticsService.getDashboard(societyId);

            assertThat(dashboard.getSlaComplianceRate()).isEqualTo(100.0);
            assertThat(dashboard.getResidentSatisfactionRate()).isEqualTo(100.0);
        }

        @Test @DisplayName("Should return top 5 vendors sorted by rating")
        void getDashboard_topVendorsSortedByRating() {
            ComplaintAnalyticsClient.ComplaintSummary summary = new ComplaintAnalyticsClient.ComplaintSummary();
            ComplaintAnalyticsClient.ComplaintTrend trend =
                    ComplaintAnalyticsClient.ComplaintTrend.builder().daily(List.of()).build();

            List<VendorAnalyticsClient.VendorPerformance> vendors = List.of(
                    VendorAnalyticsClient.VendorPerformance.builder()
                            .vendorId(UUID.randomUUID()).businessName("Vendor A")
                            .totalJobs(10).completedJobs(8).rating(BigDecimal.valueOf(4.5)).build(),
                    VendorAnalyticsClient.VendorPerformance.builder()
                            .vendorId(UUID.randomUUID()).businessName("Vendor B")
                            .totalJobs(5).completedJobs(5).rating(BigDecimal.valueOf(5.0)).build(),
                    VendorAnalyticsClient.VendorPerformance.builder()
                            .vendorId(UUID.randomUUID()).businessName("Vendor C")
                            .totalJobs(20).completedJobs(14).rating(BigDecimal.valueOf(3.8)).build()
            );

            when(complaintClient.getComplaintSummary(societyId)).thenReturn(summary);
            when(complaintClient.getComplaintTrend(eq(societyId), anyInt())).thenReturn(trend);
            when(vendorClient.getVendorPerformance(societyId)).thenReturn(vendors);

            DashboardResponse dashboard = analyticsService.getDashboard(societyId);

            assertThat(dashboard.getTopVendors()).hasSize(3);
            assertThat(dashboard.getTopVendors().get(0).getBusinessName()).isEqualTo("Vendor B");
            assertThat(dashboard.getTopVendors().get(1).getBusinessName()).isEqualTo("Vendor A");
        }
    }

    @Nested @DisplayName("refreshDailyStats()")
    class RefreshTests {

        @Test @DisplayName("Should create new daily stat record when none exists")
        void refreshDailyStats_createsNewRecord() {
            ComplaintAnalyticsClient.ComplaintSummary summary =
                    ComplaintAnalyticsClient.ComplaintSummary.builder()
                            .total(50).open(10).closed(35).slaBreaches(2)
                            .avgResolutionHours(22.0).build();

            when(complaintClient.getComplaintSummary(societyId)).thenReturn(summary);
            when(dailyStatsRepo.findBySocietyIdAndStatDate(eq(societyId), any()))
                    .thenReturn(Optional.empty());
            when(dailyStatsRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            analyticsService.refreshDailyStats(societyId);

            verify(dailyStatsRepo).save(argThat(stats ->
                    stats.getTotalComplaints() == 50 &&
                            stats.getSlaBreaches() == 2 &&
                            stats.getSocietyId().equals(societyId)));
        }

        @Test @DisplayName("Should update existing daily stat record for today")
        void refreshDailyStats_updatesExistingRecord() {
            DailyComplaintStats existing = DailyComplaintStats.builder()
                    .id(UUID.randomUUID()).societyId(societyId)
                    .statDate(LocalDate.now()).totalComplaints(40).build();

            ComplaintAnalyticsClient.ComplaintSummary summary =
                    ComplaintAnalyticsClient.ComplaintSummary.builder()
                            .total(55).open(12).closed(38).slaBreaches(3)
                            .avgResolutionHours(20.0).build();

            when(complaintClient.getComplaintSummary(societyId)).thenReturn(summary);
            when(dailyStatsRepo.findBySocietyIdAndStatDate(eq(societyId), any()))
                    .thenReturn(Optional.of(existing));
            when(dailyStatsRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            analyticsService.refreshDailyStats(societyId);

            assertThat(existing.getTotalComplaints()).isEqualTo(55);
            assertThat(existing.getSlaBreaches()).isEqualTo(3);
        }
    }
}