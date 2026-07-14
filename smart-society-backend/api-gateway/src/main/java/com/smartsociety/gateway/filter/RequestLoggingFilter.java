package com.smartsociety.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return -200;  // runs before JWT filter — logs even rejected requests
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String method    = request.getMethod().name();
        String path      = request.getPath().toString();
        String clientIp  = extractClientIp(request);
        long   startTime = System.currentTimeMillis();

        // Attach request ID for end-to-end tracing across services
        exchange.getAttributes().put("requestId", requestId);

        ServerHttpRequest mutated = request.mutate()
                .header("X-Request-Id", requestId)
                .build();

        log.info("[{}] --> {} {} from {}", requestId, method, path, clientIp);

        return chain.filter(exchange.mutate().request(mutated).build())
                .doFinally(signal -> {
                    ServerHttpResponse response = exchange.getResponse();
                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value() : 0;
                    long duration = System.currentTimeMillis() - startTime;

                    if (statusCode >= 500) {
                        log.error("[{}] <-- {} {} {} {}ms", requestId, statusCode, method, path, duration);
                    } else if (statusCode >= 400) {
                        log.warn("[{}] <-- {} {} {} {}ms", requestId, statusCode, method, path, duration);
                    } else {
                        log.info("[{}] <-- {} {} {} {}ms", requestId, statusCode, method, path, duration);
                    }
                });
    }

    private String extractClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}