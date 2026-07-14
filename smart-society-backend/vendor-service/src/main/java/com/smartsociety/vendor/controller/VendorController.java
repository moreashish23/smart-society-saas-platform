package com.smartsociety.vendor.controller;

import com.smartsociety.vendor.dto.request.*;
import com.smartsociety.vendor.dto.response.*;
import com.smartsociety.vendor.entity.*;
import com.smartsociety.vendor.exception.AccessDeniedException;
import com.smartsociety.vendor.security.RequestContext;
import com.smartsociety.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor Management", description = "Register, manage, track jobs, and rate vendors")
public class VendorController {

    private final VendorService  vendorService;
    private final RequestContext requestContext;

    // ── REGISTER ──────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Register a vendor in a society [SOCIETY_MANAGER or SUPER_ADMIN]")
    public ResponseEntity<ApiResponse<VendorResponse>> registerVendor(
            @Valid @RequestBody CreateVendorRequest request) {

        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();

        VendorResponse response = vendorService.registerVendor(
                request, requestContext.getSocietyId(), requestContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Vendor registered successfully"));
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List vendors in the society")
    public ResponseEntity<ApiResponse<PagedResponse<VendorResponse>>> getVendors(
            @RequestParam(required = false) VendorStatus    status,
            @RequestParam(required = false) ServiceCategory category,
            @RequestParam(required = false) String          keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<VendorResponse> result = vendorService.getVendors(
                requestContext.getSocietyId(), status, category, keyword,
                PageRequest.of(page, size, Sort.by("rating").descending()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── GET ONE ───────────────────────────────────────────────────────────────

    @GetMapping("/{vendorId}")
    @Operation(summary = "Get vendor profile by ID")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendor(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.getVendor(vendorId, requestContext.getSocietyId())));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my vendor profile [VENDOR role]")
    public ResponseEntity<ApiResponse<VendorResponse>> getMyProfile() {
        if (!requestContext.isVendor()) throw new AccessDeniedException();
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.getVendorByUserId(requestContext.getUserId(),
                        requestContext.getSocietyId())));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @PutMapping("/{vendorId}")
    @Operation(summary = "Update vendor profile [VENDOR(own) or Manager+]")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendor(
            @PathVariable UUID vendorId,
            @Valid @RequestBody UpdateVendorRequest request) {

        if (!requestContext.isManagerOrAbove() && !requestContext.isVendor())
            throw new AccessDeniedException();

        return ResponseEntity.ok(ApiResponse.success(
                vendorService.updateVendor(vendorId, request,
                        requestContext.getUserId(), requestContext.getSocietyId()),
                "Vendor updated successfully"));
    }

    // ── STATUS MANAGEMENT ─────────────────────────────────────────────────────

    @PatchMapping("/{vendorId}/approve")
    @Operation(summary = "Approve a pending vendor [SOCIETY_MANAGER or SUPER_ADMIN]")
    public ResponseEntity<ApiResponse<VendorResponse>> approveVendor(@PathVariable UUID vendorId) {
        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.approveVendor(vendorId, requestContext.getUserId(),
                        requestContext.getSocietyId()),
                "Vendor approved"));
    }

    @PatchMapping("/{vendorId}/suspend")
    @Operation(summary = "Suspend a vendor [SOCIETY_MANAGER or SUPER_ADMIN]")
    public ResponseEntity<ApiResponse<VendorResponse>> suspendVendor(@PathVariable UUID vendorId) {
        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.suspendVendor(vendorId, requestContext.getUserId(),
                        requestContext.getSocietyId()),
                "Vendor suspended"));
    }

    @PatchMapping("/{vendorId}/activate")
    @Operation(summary = "Reactivate a suspended vendor [SOCIETY_MANAGER or SUPER_ADMIN]")
    public ResponseEntity<ApiResponse<VendorResponse>> activateVendor(@PathVariable UUID vendorId) {
        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.activateVendor(vendorId, requestContext.getUserId(),
                        requestContext.getSocietyId()),
                "Vendor activated"));
    }

    // ── JOBS ──────────────────────────────────────────────────────────────────

    @PostMapping("/{vendorId}/jobs")
    @Operation(summary = "Record a job assignment [SOCIETY_MANAGER or SUPER_ADMIN]")
    public ResponseEntity<ApiResponse<VendorJobResponse>> recordJob(
            @PathVariable UUID vendorId,
            @Valid @RequestBody RecordJobRequest request) {

        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();
        VendorJobResponse response = vendorService.recordJob(
                vendorId, request, requestContext.getSocietyId(), requestContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Job recorded"));
    }

    @PostMapping("/jobs")
    @Operation(summary = "Create a vendor job by the assignee's user ID "
            + "[SOCIETY_MANAGER or SUPER_ADMIN — cross-service entry point used by "
            + "complaint-service when a manager assigns a complaint]")
    public ResponseEntity<ApiResponse<VendorJobResponse>> createJobForUser(
            @Valid @RequestBody CreateJobByUserRequest request) {

        if (!requestContext.isManagerOrAbove()) throw new AccessDeniedException();
        VendorJobResponse response = vendorService.createJobForUser(
                request, requestContext.getSocietyId(), requestContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Job created"));
    }

    @GetMapping("/{vendorId}/jobs")
    @Operation(summary = "Get job history for a vendor")
    public ResponseEntity<ApiResponse<PagedResponse<VendorJobResponse>>> getJobs(
            @PathVariable UUID vendorId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                vendorService.getVendorJobs(vendorId, requestContext.getSocietyId(),
                        PageRequest.of(page, size, Sort.by("assignedAt").descending()))));
    }

    @PatchMapping("/{vendorId}/jobs/{jobId}/status")
    @Operation(summary = "Update job status [VENDOR or Manager+]")
    public ResponseEntity<ApiResponse<VendorJobResponse>> updateJobStatus(
            @PathVariable UUID vendorId,
            @PathVariable UUID jobId,
            @RequestParam JobStatus newStatus) {

        if (!requestContext.isVendor() && !requestContext.isManagerOrAbove())
            throw new AccessDeniedException();

        return ResponseEntity.ok(ApiResponse.success(
                vendorService.updateJobStatus(vendorId, jobId, newStatus,
                        requestContext.getUserId(), requestContext.getSocietyId()),
                "Job status updated"));
    }

    // ── RATINGS ───────────────────────────────────────────────────────────────

    @PostMapping("/{vendorId}/ratings")
    @Operation(summary = "Rate a vendor after job completion [RESIDENT]")
    public ResponseEntity<ApiResponse<VendorRatingResponse>> rateVendor(
            @PathVariable UUID vendorId,
            @Valid @RequestBody RateVendorRequest request) {

        if (!requestContext.isResident()) throw new AccessDeniedException();

        VendorRatingResponse response = vendorService.rateVendor(
                vendorId, request, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Rating submitted successfully"));
    }

    @GetMapping("/{vendorId}/ratings")
    @Operation(summary = "Get all ratings for a vendor")
    public ResponseEntity<ApiResponse<PagedResponse<VendorRatingResponse>>> getRatings(
            @PathVariable UUID vendorId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                vendorService.getVendorRatings(vendorId, requestContext.getSocietyId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
}