package com.smartsociety.society.controller;

import com.smartsociety.society.dto.request.AddMemberRequest;
import com.smartsociety.society.dto.response.ApiResponse;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.dto.response.SocietyMemberResponse;
import com.smartsociety.society.entity.MemberStatus;
import com.smartsociety.society.exception.AccessDeniedException;
import com.smartsociety.society.security.RequestContext;
import com.smartsociety.society.service.MemberService;
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
@RequestMapping("/api/societies/{societyId}/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "Add, update, and remove society members")
public class MemberController {

    private final MemberService  memberService;
    private final RequestContext requestContext;

    @PostMapping
    @Operation(summary = "Add a member to a society [SUPER_ADMIN or SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<SocietyMemberResponse>> addMember(
            @PathVariable UUID societyId,
            @Valid @RequestBody AddMemberRequest request) {

        requireManagerOrAbove(societyId);
        SocietyMemberResponse response =
                memberService.addMember(societyId, request, requestContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Member added successfully"));
    }

    @GetMapping
    @Operation(summary = "List members of a society [Manager+]")
    public ResponseEntity<ApiResponse<PagedResponse<SocietyMemberResponse>>> getMembers(
            @PathVariable UUID societyId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        requireSocietyAccess(societyId);
        PagedResponse<SocietyMemberResponse> result = memberService.getMembers(
                societyId, role, status, PageRequest.of(page, size, Sort.by("joinedAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "Get a specific member")
    public ResponseEntity<ApiResponse<SocietyMemberResponse>> getMember(
            @PathVariable UUID societyId,
            @PathVariable UUID memberId) {

        requireSocietyAccess(societyId);
        return ResponseEntity.ok(ApiResponse.success(memberService.getMember(societyId, memberId)));
    }

    @PatchMapping("/{memberId}/role")
    @Operation(summary = "Update member role [SUPER_ADMIN or SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<SocietyMemberResponse>> updateMemberRole(
            @PathVariable UUID societyId,
            @PathVariable UUID memberId,
            @RequestParam String newRole) {

        requireManagerOrAbove(societyId);
        SocietyMemberResponse response =
                memberService.updateMemberRole(societyId, memberId, newRole, requestContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Member role updated"));
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "Remove a member from society [SUPER_ADMIN or SOCIETY_MANAGER]")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID societyId,
            @PathVariable UUID memberId) {

        requireManagerOrAbove(societyId);
        memberService.removeMember(societyId, memberId, requestContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed successfully"));
    }


    private void requireManagerOrAbove(UUID societyId) {
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