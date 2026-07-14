package com.smartsociety.society.controller;

import com.smartsociety.society.dto.request.CreateNoticeRequest;
import com.smartsociety.society.dto.request.UpdateNoticeRequest;
import com.smartsociety.society.dto.response.ApiResponse;
import com.smartsociety.society.dto.response.NoticeResponse;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.entity.NoticeStatus;
import com.smartsociety.society.entity.NoticeType;
import com.smartsociety.society.exception.AccessDeniedException;
import com.smartsociety.society.security.RequestContext;
import com.smartsociety.society.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/societies/{societyId}/notices")
@RequiredArgsConstructor
@Tag(name = "Notice Board", description = "Publish and manage society notices")
public class NoticeController {

    private final NoticeService  noticeService;
    private final RequestContext requestContext;

    @PostMapping
    @Operation(summary = "Create a notice [SOCIETY_MANAGER or COMMITTEE_MEMBER]")
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @PathVariable UUID societyId,
            @Valid @RequestBody CreateNoticeRequest request) {

        requireManagerOrCommittee(societyId);
        NoticeResponse response = noticeService.createNotice(societyId, request, requestContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notice created successfully"));
    }

    @GetMapping
    @Operation(summary = "List notices for a society (all members can view published)")
    public ResponseEntity<ApiResponse<PagedResponse<NoticeResponse>>> getNotices(
            @PathVariable UUID societyId,
            @RequestParam(required = false) NoticeStatus status,
            @RequestParam(required = false) NoticeType type,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        requireSocietyAccess(societyId);

        NoticeStatus effectiveStatus = (!requestContext.isManager()
                && !requestContext.isCommitteeMember()
                && !requestContext.isSuperAdmin())
                ? NoticeStatus.PUBLISHED : status;

        PagedResponse<NoticeResponse> result = noticeService.getNotices(
                societyId, effectiveStatus, type,
                PageRequest.of(page, size, Sort.by("priority").descending()
                        .and(Sort.by("createdAt").descending())));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active (published, non-expired) notices")
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getActiveNotices(
            @PathVariable UUID societyId) {

        requireSocietyAccess(societyId);
        return ResponseEntity.ok(ApiResponse.success(noticeService.getActiveNotices(societyId)));
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "Get a specific notice")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
            @PathVariable UUID societyId,
            @PathVariable UUID noticeId) {

        requireSocietyAccess(societyId);
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotice(societyId, noticeId)));
    }

    @PutMapping("/{noticeId}")
    @Operation(summary = "Update a notice [SOCIETY_MANAGER or COMMITTEE_MEMBER]")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable UUID societyId,
            @PathVariable UUID noticeId,
            @Valid @RequestBody UpdateNoticeRequest request) {

        requireManagerOrCommittee(societyId);
        return ResponseEntity.ok(ApiResponse.success(
                noticeService.updateNotice(societyId, noticeId, request, requestContext.getUserId()),
                "Notice updated"));
    }

    @PatchMapping("/{noticeId}/publish")
    @Operation(summary = "Publish a draft notice [SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<NoticeResponse>> publishNotice(
            @PathVariable UUID societyId,
            @PathVariable UUID noticeId) {

        requireManagerOrCommittee(societyId);
        return ResponseEntity.ok(ApiResponse.success(
                noticeService.publishNotice(societyId, noticeId, requestContext.getUserId()),
                "Notice published"));
    }

    @PatchMapping("/{noticeId}/archive")
    @Operation(summary = "Archive a notice [SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<NoticeResponse>> archiveNotice(
            @PathVariable UUID societyId,
            @PathVariable UUID noticeId) {

        requireManagerOrCommittee(societyId);
        return ResponseEntity.ok(ApiResponse.success(
                noticeService.archiveNotice(societyId, noticeId, requestContext.getUserId()),
                "Notice archived"));
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "Delete a notice [SUPER_ADMIN or SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable UUID societyId,
            @PathVariable UUID noticeId) {

        requireManagerOrCommittee(societyId);
        noticeService.deleteNotice(societyId, noticeId, requestContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notice deleted"));
    }


    private void requireManagerOrCommittee(UUID societyId) {
        if (requestContext.isSuperAdmin()) return;
        if ((requestContext.isManager() || requestContext.isCommitteeMember())
                && societyId.equals(requestContext.getSocietyId())) return;
        throw new AccessDeniedException();
    }

    private void requireSocietyAccess(UUID societyId) {
        if (requestContext.isSuperAdmin()) return;
        if (societyId.equals(requestContext.getSocietyId())) return;
        throw new AccessDeniedException();
    }
}