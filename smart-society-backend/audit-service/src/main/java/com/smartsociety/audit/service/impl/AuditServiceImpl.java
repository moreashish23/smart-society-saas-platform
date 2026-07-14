package com.smartsociety.audit.service.impl;

import com.smartsociety.audit.dto.request.LogEventRequest;
import com.smartsociety.audit.dto.response.AuditLogResponse;
import com.smartsociety.audit.dto.response.PagedResponse;
import com.smartsociety.audit.entity.AuditAction;
import com.smartsociety.audit.entity.AuditLog;
import com.smartsociety.audit.exception.AuditLogNotFoundException;
import com.smartsociety.audit.mapper.AuditLogMapper;
import com.smartsociety.audit.repository.AuditLogRepository;
import com.smartsociety.audit.repository.AuditLogSpecifications;
import com.smartsociety.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper     auditLogMapper;


    @Override
    @Transactional
    public AuditLogResponse logEvent(LogEventRequest request) {
        AuditLog loga = AuditLog.builder()
                .userId(request.getUserId())
                .societyId(request.getSocietyId())
                .action(request.getAction())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .description(request.getDescription())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .build();

        AuditLog saved = auditLogRepository.save(loga);

        log.debug("Audit log saved: id={}, action={}, userId={}, societyId={}",
                saved.getId(), saved.getAction(), saved.getUserId(), saved.getSocietyId());

        return auditLogMapper.toResponse(saved);
    }

    // ── GET SINGLE ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getLog(UUID logId) {
        return auditLogRepository.findById(logId)
                .map(auditLogMapper::toResponse)
                .orElseThrow(() -> new AuditLogNotFoundException(logId.toString()));
    }

    // ── SEARCH

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getLogs(UUID societyId, UUID userId,
                                                   AuditAction action, String entityType,
                                                   UUID entityId, LocalDateTime from,
                                                   LocalDateTime to, Pageable pageable) {

        var spec = AuditLogSpecifications.search(societyId, userId, action, entityType, from, to);
        return PagedResponse.of(
                auditLogRepository.findAll(spec, pageable)
                        .map(auditLogMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getLogsByEntity(UUID societyId, String entityType,
                                                           UUID entityId, Pageable pageable) {
        return PagedResponse.of(
                auditLogRepository.findBySocietyIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                                societyId, entityType, entityId, pageable)
                        .map(auditLogMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getLogsByUser(UUID userId, Pageable pageable) {
        return PagedResponse.of(
                auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                        .map(auditLogMapper::toResponse));
    }

    // ── COUNT ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long countBySociety(UUID societyId) {
        return auditLogRepository.countBySocietyId(societyId);
    }

    // ── RETENTION PURGE ───────────────────────────────────────────────────────


    @Override
    @Transactional
    public int purgeOldLogs(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = auditLogRepository.deleteLogsOlderThan(cutoff);
        log.info("Audit retention purge: deleted {} logs older than {} days (cutoff={})",
                deleted, retentionDays, cutoff);
        return deleted;
    }
}