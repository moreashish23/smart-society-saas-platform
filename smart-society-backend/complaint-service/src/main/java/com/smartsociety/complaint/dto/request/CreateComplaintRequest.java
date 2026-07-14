package com.smartsociety.complaint.dto.request;

import com.smartsociety.complaint.entity.ComplaintCategory;
import com.smartsociety.complaint.entity.ComplaintPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateComplaintRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be 5-255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    private String description;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    private ComplaintPriority priority;

    @Size(max = 255)
    private String location;
}