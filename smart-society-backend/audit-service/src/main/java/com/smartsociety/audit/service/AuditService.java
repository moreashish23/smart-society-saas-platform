package com.smartsociety.audit.service;

import com.smartsociety.audit.dto.request.LogEventRequest;
import com.smartsociety.audit.dto.response.AuditLogResponse;
import com.smartsociety.audit.dto.response.PagedResponse;
import com.smartsociety.audit.entity.AuditAction;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditService {

    AuditLogResponse logEvent(LogEventRequest request);

    AuditLogResponse getLog(UUID logId);

    PagedResponse<AuditLogResponse> getLogs(UUID societyId, UUID userId, AuditAction action,
                                            String entityType, UUID entityId,
                                            LocalDateTime from, LocalDateTime to,
                                            Pageable pageable);

    PagedResponse<AuditLogResponse> getLogsByEntity(UUID societyId, String entityType,
                                                    UUID entityId, Pageable pageable);

    PagedResponse<AuditLogResponse> getLogsByUser(UUID userId, Pageable pageable);

    long countBySociety(UUID societyId);

    int purgeOldLogs(int retentionDays);
}