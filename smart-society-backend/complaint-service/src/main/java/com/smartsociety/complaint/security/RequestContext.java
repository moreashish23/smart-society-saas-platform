package com.smartsociety.complaint.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestContext {

    private final HttpServletRequest request;

    public UUID getUserId() {
        String val = request.getHeader("X-User-Id");
        if (val == null || val.isBlank()) throw new IllegalStateException("X-User-Id header missing");
        return UUID.fromString(val);
    }

    public UUID getSocietyId() {
        String val = request.getHeader("X-Society-Id");
        if (val == null || val.isBlank()) throw new IllegalStateException("X-Society-Id header missing");
        return UUID.fromString(val);
    }

    public String getRole() {
        return request.getHeader("X-User-Role");
    }

    public boolean isSuperAdmin()      { return "SUPER_ADMIN".equals(getRole()); }
    public boolean isManager()         { return "SOCIETY_MANAGER".equals(getRole()); }
    public boolean isCommitteeMember() { return "COMMITTEE_MEMBER".equals(getRole()); }
    public boolean isResident()        { return "RESIDENT".equals(getRole()); }
    public boolean isVendor()          { return "VENDOR".equals(getRole()); }
    public boolean isStaff()           { return "MAINTENANCE_STAFF".equals(getRole()); }

    public boolean isManagerOrAbove() {
        return isSuperAdmin() || isManager() || isCommitteeMember();
    }
}