package com.smartsociety.audit.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestContext {

    private final HttpServletRequest request;

    public UUID getUserId() {
        String v = request.getHeader("X-User-Id");
        return v != null && !v.isBlank() ? UUID.fromString(v) : null;
    }

    public UUID getSocietyId() {
        String v = request.getHeader("X-Society-Id");
        return v != null && !v.isBlank() ? UUID.fromString(v) : null;
    }

    public String getRole() {
        return request.getHeader("X-User-Role");
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getRole());
    }

    public boolean isManagerOrAbove() {
        String r = getRole();
        return "SUPER_ADMIN".equals(r)
                || "SOCIETY_MANAGER".equals(r)
                || "COMMITTEE_MEMBER".equals(r);
    }
}