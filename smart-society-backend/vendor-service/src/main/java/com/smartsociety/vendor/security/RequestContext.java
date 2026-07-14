package com.smartsociety.vendor.security;

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
        String v = header("X-User-Id");
        if (v == null) v = (String) request.getAttribute("userId");
        return v != null ? UUID.fromString(v) : null;
    }

    public String getRole() {
        String v = header("X-User-Role");
        if (v == null) v = (String) request.getAttribute("role");
        return v;
    }

    public UUID getSocietyId() {
        String v = header("X-Society-Id");
        if (v == null) v = (String) request.getAttribute("societyId");
        return (v != null && !v.isBlank()) ? UUID.fromString(v) : null;
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

    private String header(String name) {
        String v = request.getHeader(name);
        return (v != null && !v.isBlank()) ? v : null;
    }
}