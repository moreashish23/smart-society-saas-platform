package com.smartsociety.complaint.controller;

import com.smartsociety.complaint.dto.request.*;
import com.smartsociety.complaint.dto.response.*;
import com.smartsociety.complaint.entity.*;
import com.smartsociety.complaint.exception.AccessDeniedException;
import com.smartsociety.complaint.security.RequestContext;
import com.smartsociety.complaint.service.ComplaintService;
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
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaint Management", description = "Full complaint lifecycle: creation, assignment, SLA, escalation, verification")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final RequestContext   requestContext;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a complaint [RESIDENT]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request) {

        if (!requestContext.isResident()) throw new AccessDeniedException();

        ComplaintResponse response = complaintService.createComplaint(
                request, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Complaint submitted successfully"));
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List complaints [Manager+ sees all; Resident sees own]")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> getComplaints(
            @RequestParam(required = false) ComplaintStatus   status,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) ComplaintPriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID societyId = requestContext.getSocietyId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        PagedResponse<ComplaintResponse> result;

        if (requestContext.isManagerOrAbove()) {
            result = complaintService.getComplaints(societyId, status, category, priority, keyword, pageable);
        } else if (requestContext.isResident()) {
            result = complaintService.getMyComplaints(requestContext.getUserId(), societyId, pageable);
        } else if (requestContext.isVendor() || requestContext.isStaff()) {
            result = complaintService.getAssignedComplaints(requestContext.getUserId(), pageable);
        } else {
            throw new AccessDeniedException();
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my complaints [RESIDENT]")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> getMyComplaints(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (!requestContext.isResident()) throw new AccessDeniedException();

        PagedResponse<ComplaintResponse> result = complaintService.getMyComplaints(
                requestContext.getUserId(), requestContext.getSocietyId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/assigned")
    @Operation(summary = "Get complaints assigned to me [VENDOR or MAINTENANCE_STAFF]")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> getAssignedToMe(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (!requestContext.isVendor() && !requestContext.isStaff()) throw new AccessDeniedException();

        PagedResponse<ComplaintResponse> result = complaintService.getAssignedComplaints(
                requestContext.getUserId(),
                PageRequest.of(page, size, Sort.by("slaDeadline").ascending()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── GET ONE ───────────────────────────────────────────────────────────────

    @GetMapping("/{complaintId}")
    @Operation(summary = "Get complaint detail with full timeline and comments")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaint(
            @PathVariable UUID complaintId) {

        ComplaintResponse response = complaintService.getComplaint(
                complaintId, requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── ASSIGN ────────────────────────────────────────────────────────────────

    @PatchMapping("/{complaintId}/assign")
    @Operation(summary = "Assign complaint to vendor/staff [SOCIETY_MANAGER or COMMITTEE_MEMBER]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignComplaint(
            @PathVariable UUID complaintId,
            @Valid @RequestBody AssignComplaintRequest request) {

        if (!requestContext.isManager() && !requestContext.isCommitteeMember()
                && !requestContext.isSuperAdmin()) throw new AccessDeniedException();

        ComplaintResponse response = complaintService.assignComplaint(
                complaintId, request, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response, "Complaint assigned successfully"));
    }

    // ── VENDOR WORKFLOW ───────────────────────────────────────────────────────

    @PatchMapping("/{complaintId}/accept")
    @Operation(summary = "Accept an assigned complaint [VENDOR or MAINTENANCE_STAFF]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> acceptComplaint(
            @PathVariable UUID complaintId) {

        if (!requestContext.isVendor() && !requestContext.isStaff()) throw new AccessDeniedException();

        ComplaintResponse response = complaintService.acceptComplaint(
                complaintId, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response, "Complaint accepted"));
    }

    @PatchMapping("/{complaintId}/start-work")
    @Operation(summary = "Mark work as started [VENDOR or MAINTENANCE_STAFF]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> startWork(
            @PathVariable UUID complaintId) {

        if (!requestContext.isVendor() && !requestContext.isStaff()) throw new AccessDeniedException();

        ComplaintResponse response = complaintService.startWork(
                complaintId, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response, "Work started"));
    }

    @PatchMapping("/{complaintId}/complete-work")
    @Operation(summary = "Mark work as completed [VENDOR or MAINTENANCE_STAFF]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> completeWork(
            @PathVariable UUID complaintId) {

        if (!requestContext.isVendor() && !requestContext.isStaff()) throw new AccessDeniedException();

        ComplaintResponse response = complaintService.markWorkCompleted(
                complaintId, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response, "Work marked as completed"));
    }

    // ── RESOLVE ───────────────────────────────────────────────────────────────

    @PatchMapping("/{complaintId}/resolve")
    @Operation(summary = "Mark complaint as resolved — triggers resident verification [SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolveComplaint(
            @PathVariable UUID complaintId,
            @Valid @RequestBody ResolveComplaintRequest request) {

        if (!requestContext.isManager() && !requestContext.isSuperAdmin()) throw new AccessDeniedException();

        ComplaintResponse response = complaintService.markResolved(
                complaintId, request, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response,
                "Complaint marked as resolved. Resident will be notified to verify."));
    }

    // ── RESIDENT VERIFICATION ─────────────────────────────────────────────────

    @PostMapping("/{complaintId}/verify")
    @Operation(summary = "Resident verifies resolution: YES=close, NO=reopen [RESIDENT]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> verifyResolution(
            @PathVariable UUID complaintId,
            @RequestParam boolean resolved) {

        if (!requestContext.isResident()) throw new AccessDeniedException();

        ComplaintResponse response = complaintService.verifyResolution(
                complaintId, resolved, requestContext.getUserId(), requestContext.getSocietyId());

        String message = resolved
                ? "Complaint closed. Thank you for your feedback!"
                : "Complaint reopened. The team will follow up again.";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    // ── CANCEL ────────────────────────────────────────────────────────────────

    @PatchMapping("/{complaintId}/cancel")
    @Operation(summary = "Cancel a complaint [RESIDENT (own) or Manager+]")
    public ResponseEntity<ApiResponse<ComplaintResponse>> cancelComplaint(
            @PathVariable UUID complaintId) {

        ComplaintResponse response = complaintService.cancelComplaint(
                complaintId, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response, "Complaint cancelled"));
    }

    // ── COMMENTS ──────────────────────────────────────────────────────────────

    @PostMapping("/{complaintId}/comments")
    @Operation(summary = "Add a comment to a complaint")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable UUID complaintId,
            @Valid @RequestBody AddCommentRequest request) {

        CommentResponse response = complaintService.addComment(
                complaintId, request,
                requestContext.getUserId(), requestContext.getRole(),
                requestContext.getSocietyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Comment added"));
    }

    @GetMapping("/{complaintId}/comments")
    @Operation(summary = "Get all comments on a complaint")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable UUID complaintId) {

        List<CommentResponse> comments = complaintService.getComments(
                complaintId, requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
}