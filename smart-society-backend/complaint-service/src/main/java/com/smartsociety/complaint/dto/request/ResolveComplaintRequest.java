package com.smartsociety.complaint.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveComplaintRequest {

    @NotBlank(message = "Resolution note is required")
    private String resolutionNote;
}