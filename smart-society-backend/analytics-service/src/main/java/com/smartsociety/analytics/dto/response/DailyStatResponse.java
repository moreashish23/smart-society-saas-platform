package com.smartsociety.analytics.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyStatResponse {
    private LocalDate  date;
    private int        totalComplaints;
    private int        openComplaints;
    private int        closedComplaints;
    private int        slaBreaches;
    private BigDecimal avgResolutionHours;
}