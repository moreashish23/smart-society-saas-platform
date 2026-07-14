package com.smartsociety.vendor.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobByUserRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private UUID complaintId;

    private String complaintTitle;
    private String notes;
}