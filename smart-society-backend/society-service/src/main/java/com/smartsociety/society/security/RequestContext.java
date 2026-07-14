package com.smartsociety.society.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;


@Component
@RequestScope
@RequiredArgsConstructor
public class RequestContext {

    private final HttpServletRequest request;

    public UUID getUserId() {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            userId = (String) request.getAttribute("userId");
        }
        return userId != null ? UUID.fromString(userId) : null;
    }

    public String getRole() {
        String role = request.getHeader("X-User-Role");
        if (role == null || role.isBlank()) {
            role = (String) request.getAttribute("role");
        }
        return role;
    }

    public UUID getSocietyId() {
        String sid = request.getHeader("X-Society-Id");
        if (sid == null || sid.isBlank()) {
            sid = (String) request.getAttribute("societyId");
        }
        return (sid != null && !sid.isBlank()) ? UUID.fromString(sid) : null;
    }

    public boolean isSuperAdmin()      { return "SUPER_ADMIN".equals(getRole()); }
    public boolean isManager()         { return "SOCIETY_MANAGER".equals(getRole()); }
    public boolean isCommitteeMember() { return "COMMITTEE_MEMBER".equals(getRole()); }
    public boolean isResident()        { return "RESIDENT".equals(getRole()); }
    public boolean isVendor()          { return "VENDOR".equals(getRole()); }
    public boolean isStaff()           { return "MAINTENANCE_STAFF".equals(getRole()); }
}