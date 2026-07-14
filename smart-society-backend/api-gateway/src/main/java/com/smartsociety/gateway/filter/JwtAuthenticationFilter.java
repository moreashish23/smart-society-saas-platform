package com.smartsociety.gateway.filter;

import com.smartsociety.gateway.config.GatewayAppProperties;
import com.smartsociety.gateway.security.GatewayJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter
        extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final GatewayJwtUtil       jwtUtil;
    private final GatewayAppProperties gatewayAppProperties;
    private final AntPathMatcher        pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(GatewayJwtUtil jwtUtil,
                                   GatewayAppProperties gatewayAppProperties) {
        super(Config.class);
        this.jwtUtil              = jwtUtil;
        this.gatewayAppProperties = gatewayAppProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // Skip JWT validation for public paths
            boolean isPublic = gatewayAppProperties.getPublicPaths().stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));

            if (isPublic) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or malformed Authorization header for path: {}", path);
                return unauthorised(exchange);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.isValid(token)) {
                log.warn("Invalid or expired JWT for path: {}", path);
                return unauthorised(exchange);
            }

            String userId    = jwtUtil.getUserId(token);
            String role      = jwtUtil.getRole(token);
            String societyId = jwtUtil.getSocietyId(token);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id",    userId    != null ? userId    : "")
                    .header("X-User-Role",  role      != null ? role      : "")
                    .header("X-Society-Id", societyId != null ? societyId : "")
                    .build();

            log.debug("JWT validated — userId={}, role={}, societyId={}, path={}",
                    userId, role, societyId, path);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private Mono<Void> unauthorised(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    public static class Config {
        // Intentionally empty — filter uses GatewayAppProperties, not per-route config
    }
}