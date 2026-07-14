package com.smartsociety.vendor.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorRatingResponse {
    private UUID          id;
    private UUID          vendorId;
    private UUID          societyId;
    private UUID          complaintId;
    private UUID          ratedBy;
    private BigDecimal    rating;
    private String        review;
    private LocalDateTime createdAt;
}