package com.smartsociety.notification.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestContext {
    private final HttpServletRequest request;
    public UUID getUserId()    { return UUID.fromString(request.getHeader("X-User-Id")); }
    public UUID getSocietyId() { return UUID.fromString(request.getHeader("X-Society-Id")); }
    public String getRole()    { return request.getHeader("X-User-Role"); }
    public boolean isManagerOrAbove() {
        String r = getRole();
        return "SUPER_ADMIN".equals(r) || "SOCIETY_MANAGER".equals(r) || "COMMITTEE_MEMBER".equals(r);
    }
}