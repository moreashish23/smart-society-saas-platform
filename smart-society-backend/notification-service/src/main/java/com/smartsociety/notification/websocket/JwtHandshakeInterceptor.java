package com.smartsociety.notification.websocket;

import com.smartsociety.notification.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String token = extractToken(request);

        if (token == null || !jwtUtil.isValid(token)) {
            log.warn("WebSocket handshake rejected — invalid or missing JWT");
            return false;
        }

        String userId   = jwtUtil.getUserId(token);
        String societyId = jwtUtil.getSocietyId(token);
        String role     = jwtUtil.getRole(token);

        attributes.put("userId",    userId);
        attributes.put("societyId", societyId);
        attributes.put("role",      role);

        log.debug("WebSocket handshake accepted: userId={}, societyId={}", userId, societyId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. Try query parameter: ws://host/ws?token=xxx
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String queryToken = servletRequest.getServletRequest().getParameter("token");
            if (StringUtils.hasText(queryToken)) return queryToken;
        }

        // 2. Try Authorization header: Bearer xxx
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}