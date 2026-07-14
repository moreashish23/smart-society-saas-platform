package com.smartsociety.analytics.security;

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
    public boolean isManagerOrAbove()  {
        String r = getRole();
        return "SUPER_ADMIN".equals(r)
                || "SOCIETY_MANAGER".equals(r)
                || "COMMITTEE_MEMBER".equals(r);
    }

    private String header(String name) {
        String v = request.getHeader(name);
        return (v != null && !v.isBlank()) ? v : null;
    }
}