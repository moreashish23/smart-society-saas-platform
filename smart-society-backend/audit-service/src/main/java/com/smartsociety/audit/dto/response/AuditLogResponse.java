package com.smartsociety.audit.dto.response;

import com.smartsociety.audit.entity.AuditAction;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private UUID id;
    private UUID societyId;
    private UUID userId;
    private AuditAction action;
    private String entityType;
    private UUID entityId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}