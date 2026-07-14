package com.smartsociety.complaint.dto.response;

import com.smartsociety.complaint.entity.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {

    private UUID id;
    private UUID societyId;
    private UUID residentId;
    private String title;
    private String description;
    private ComplaintCategory category;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private UUID assignedToId;
    private LocalDateTime assignedAt;
    private LocalDateTime slaDeadline;
    private boolean slaBreached;
    private int escalationLevel;
    private LocalDateTime escalatedAt;
    private int reopenCount;
    private String resolutionNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TimelineResponse>   timeline;
    private List<CommentResponse>    comments;
    private List<AttachmentResponse> attachments;
}