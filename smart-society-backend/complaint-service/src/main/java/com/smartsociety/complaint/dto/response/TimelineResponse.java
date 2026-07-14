package com.smartsociety.complaint.dto.response;

import com.smartsociety.complaint.entity.TimelineAction;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineResponse {
    private UUID id;
    private TimelineAction action;
    private UUID performedBy;
    private String note;
    private LocalDateTime createdAt;
}