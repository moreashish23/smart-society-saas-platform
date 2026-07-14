package com.smartsociety.audit.controller;

import com.smartsociety.audit.dto.request.LogEventRequest;
import com.smartsociety.audit.dto.response.ApiResponse;
import com.smartsociety.audit.dto.response.AuditLogResponse;
import com.smartsociety.audit.dto.response.PagedResponse;
import com.smartsociety.audit.entity.AuditAction;
import com.smartsociety.audit.exception.AccessDeniedException;
import com.smartsociety.audit.security.RequestContext;
import com.smartsociety.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Write-once immutable audit trail for all platform events")
public class AuditController {

    private final AuditService   auditService;
    private final RequestContext requestContext;


    @PostMapping("/log")
    @Operation(summary = "Record an audit event [Internal — called by other microservices]",
            description = "Creates an immutable audit record. Called by Auth, Society, Complaint, Vendor services.")
    public ResponseEntity<ApiResponse<AuditLogResponse>> logEvent(
            @Valid @RequestBody LogEventRequest request) {

        AuditLogResponse response = auditService.logEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Audit event recorded"));
    }

    // ── GET SINGLE ────────────────────────────────────────────────────────────

    @GetMapping("/{logId}")
    @Operation(summary = "Get a specific audit log entry [Manager+]")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getLog(@PathVariable UUID logId) {
        requireManagerOrAbove();
        return ResponseEntity.ok(ApiResponse.success(auditService.getLog(logId)));
    }

    // ── SEARCH / FILTER ───────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Search audit logs with filters [Manager+]",
            description = "Filter by userId, action, entityType, entityId, and date range. SUPER_ADMIN can query any society.")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        requireManagerOrAbove();

        // SUPER_ADMIN can see all societies; others are scoped to their own society
        UUID societyId = requestContext.isSuperAdmin() ? null : requestContext.getSocietyId();

        PagedResponse<AuditLogResponse> result = auditService.getLogs(
                societyId, userId, action, entityType, entityId, from, to,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return ResponseEntity.ok(ApiResponse.success(result));
    }



    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get full audit trail for a specific entity [Manager+]",
            description = "e.g. GET /audit/entity/COMPLAINT/{id} returns every status change for that complaint.")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getEntityTrail(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        requireManagerOrAbove();

        UUID societyId = requestContext.isSuperAdmin() ? null : requestContext.getSocietyId();

        PagedResponse<AuditLogResponse> result = auditService.getLogsByEntity(
                societyId, entityType.toUpperCase(), entityId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── USER ACTIVITY ─────────────────────────────────────────────────────────

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit trail for a specific user [SUPER_ADMIN or self]")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getUserLogs(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        boolean isSelf = userId.equals(requestContext.getUserId());
        if (!requestContext.isManagerOrAbove() && !isSelf) throw new AccessDeniedException();

        PagedResponse<AuditLogResponse> result = auditService.getLogsByUser(
                userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get audit log statistics for a society [Manager+]")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        requireManagerOrAbove();
        long count = auditService.countBySociety(requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("totalLogs", count)));
    }


    private void requireManagerOrAbove() {
        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();
    }
}