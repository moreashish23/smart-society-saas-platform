package com.smartsociety.vendor.dto.response;

import com.smartsociety.vendor.entity.JobStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorJobResponse {
    private UUID          id;
    private UUID          vendorId;
    private UUID          societyId;
    private UUID          complaintId;
    private String        complaintTitle;
    private JobStatus     status;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String        notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}