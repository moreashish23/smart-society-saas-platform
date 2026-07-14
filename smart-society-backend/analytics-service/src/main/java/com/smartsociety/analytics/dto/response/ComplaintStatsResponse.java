package com.smartsociety.analytics.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintStatsResponse {
    private UUID              societyId;
    private LocalDate         from;
    private LocalDate         to;
    private long              totalComplaints;
    private long              openComplaints;
    private long              closedComplaints;
    private long              slaBreaches;
    private double            avgResolutionHours;
    private double            slaComplianceRate;
    private List<DailyStatResponse> dailyStats;
}