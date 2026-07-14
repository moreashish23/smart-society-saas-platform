package com.smartsociety.analytics.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPerformanceResponse {
    private UUID      vendorId;
    private String    businessName;
    private LocalDate statMonth;
    private int       jobsAssigned;
    private int       jobsCompleted;
    private int       jobsCancelled;
    private double    avgRating;
    private double    completionRate;
}