package com.smartsociety.complaint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(min = 2, max = 2000, message = "Comment must be 2-2000 characters")
    private String content;
}