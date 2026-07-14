package com.smartsociety.vendor.dto.response;

import com.smartsociety.vendor.entity.ServiceCategory;
import com.smartsociety.vendor.entity.VendorStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {
    private UUID            id;
    private UUID            societyId;
    private UUID            userId;
    private String          businessName;
    private String          contactPerson;
    private String          contactEmail;
    private String          contactPhone;
    private ServiceCategory serviceCategory;
    private String          description;
    private String          address;
    private BigDecimal      rating;
    private Integer         totalJobs;
    private Integer         completedJobs;
    private VendorStatus    status;
    private UUID            approvedBy;
    private LocalDateTime   approvedAt;
    private LocalDateTime   createdAt;
    private LocalDateTime   updatedAt;
}