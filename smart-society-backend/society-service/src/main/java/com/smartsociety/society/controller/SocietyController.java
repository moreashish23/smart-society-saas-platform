package com.smartsociety.society.controller;

import com.smartsociety.society.dto.request.CreateSocietyRequest;
import com.smartsociety.society.dto.request.UpdateSocietyRequest;
import com.smartsociety.society.dto.response.ApiResponse;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.dto.response.SocietyResponse;
import com.smartsociety.society.entity.SocietyStatus;
import com.smartsociety.society.exception.AccessDeniedException;
import com.smartsociety.society.security.RequestContext;
import com.smartsociety.society.service.SocietyService;
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
@RequestMapping("/api/societies")
@RequiredArgsConstructor
@Tag(name = "Society Management", description = "Create, update, and manage housing societies")
public class SocietyController {

    private final SocietyService  societyService;
    private final RequestContext  requestContext;


    @PostMapping
    @Operation(summary = "Create a new society [SUPER_ADMIN only]")
    public ResponseEntity<ApiResponse<SocietyResponse>> createSociety(
            @Valid @RequestBody CreateSocietyRequest request) {

        requireSuperAdmin();
        SocietyResponse response = societyService.createSociety(request, requestContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Society created successfully"));
    }


    @GetMapping("/{societyId}")
    @Operation(summary = "Get society by ID")
    public ResponseEntity<ApiResponse<SocietyResponse>> getSociety(
            @PathVariable UUID societyId) {


        if (!requestContext.isSuperAdmin()) {
            UUID mySocietyId = requestContext.getSocietyId();
            if (mySocietyId == null || !mySocietyId.equals(societyId)) {
                throw new AccessDeniedException();
            }
        }

        return ResponseEntity.ok(ApiResponse.success(societyService.getSociety(societyId)));
    }


    @GetMapping
    @Operation(summary = "List all societies [SUPER_ADMIN only]")
    public ResponseEntity<ApiResponse<PagedResponse<SocietyResponse>>> getAllSocieties(
            @RequestParam(required = false) SocietyStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        requireSuperAdmin();
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PagedResponse<SocietyResponse> result =
                societyService.getAllSocieties(status, name, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{societyId}")
    @Operation(summary = "Update society details [SUPER_ADMIN or SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<SocietyResponse>> updateSociety(
            @PathVariable UUID societyId,
            @Valid @RequestBody UpdateSocietyRequest request) {

        requireSuperAdminOrManager(societyId);
        SocietyResponse response = societyService.updateSociety(societyId, request, requestContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Society updated successfully"));
    }


    @PatchMapping("/{societyId}/activate")
    @Operation(summary = "Activate a society [SUPER_ADMIN only]")
    public ResponseEntity<ApiResponse<SocietyResponse>> activateSociety(
            @PathVariable UUID societyId) {

        requireSuperAdmin();
        return ResponseEntity.ok(ApiResponse.success(
                societyService.activateSociety(societyId, requestContext.getUserId()),
                "Society activated"));
    }

    @PatchMapping("/{societyId}/deactivate")
    @Operation(summary = "Deactivate a society [SUPER_ADMIN only]")
    public ResponseEntity<ApiResponse<SocietyResponse>> deactivateSociety(
            @PathVariable UUID societyId) {

        requireSuperAdmin();
        return ResponseEntity.ok(ApiResponse.success(
                societyService.deactivateSociety(societyId, requestContext.getUserId()),
                "Society deactivated"));
    }


    @DeleteMapping("/{societyId}")
    @Operation(summary = "Delete a society [SUPER_ADMIN only]")
    public ResponseEntity<ApiResponse<Void>> deleteSociety(@PathVariable UUID societyId) {
        requireSuperAdmin();
        societyService.deleteSociety(societyId, requestContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Society deleted successfully"));
    }

    private void requireSuperAdmin() {
        if (!requestContext.isSuperAdmin()) throw new AccessDeniedException();
    }

    private void requireSuperAdminOrManager(UUID societyId) {
        if (requestContext.isSuperAdmin()) return;
        if (requestContext.isManager()) {
            UUID mySociety = requestContext.getSocietyId();
            if (mySociety != null && mySociety.equals(societyId)) return;
        }
        throw new AccessDeniedException();
    }
}