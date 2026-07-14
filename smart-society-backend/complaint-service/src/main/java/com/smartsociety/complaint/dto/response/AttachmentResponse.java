package com.smartsociety.complaint.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private UUID id;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private UUID uploadedBy;
    private LocalDateTime createdAt;
}