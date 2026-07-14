package com.smartsociety.complaint.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignComplaintRequest {

    @NotNull(message = "assignedToId is required")
    private UUID assignedToId;

    private String note;
}