package com.smartsociety.audit.dto.request;

import com.smartsociety.audit.entity.AuditAction;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEventRequest {

    private UUID userId;
    private UUID societyId;

    @NotNull(message = "action is required")
    private AuditAction action;

    private String entityType;
    private UUID entityId;
    private String description;
    private String ipAddress;
    private String userAgent;
}