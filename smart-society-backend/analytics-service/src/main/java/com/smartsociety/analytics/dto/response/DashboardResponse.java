package com.smartsociety.analytics.dto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private UUID   societyId;
    private long   totalComplaints;
    private long   openComplaints;
    private long   closedComplaints;
    private long   inProgressComplaints;
    private long   pendingVerificationComplaints;
    private long   reopenedComplaints;
    private long   criticalComplaints;
    private long   slaBreaches;
    private double avgResolutionHours;
    private long   criticalCount;
    private long   highCount;
    private long   mediumCount;
    private long   lowCount;
    private double slaComplianceRate;
    private double residentSatisfactionRate;

    private List<VendorStat>  topVendors;
    private List<TrendPoint>  complaintTrend;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private String date;
        private long   created;
        private long   closed;
        private long   escalated;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorStat {
        private UUID       vendorId;
        private String     businessName;
        private String     serviceCategory;
        private int        totalJobs;
        private int        completedJobs;
        private double     completionRate;
        private java.math.BigDecimal rating;
    }
}