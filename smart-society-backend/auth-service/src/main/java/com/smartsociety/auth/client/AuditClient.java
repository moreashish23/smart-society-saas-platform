package com.smartsociety.auth.client;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name     = "audit-service-from-auth",
        url      = "${app.services.audit-service-url}",
        fallback = AuditClient.Fallback.class
)
public interface AuditClient {

    @PostMapping("/api/audit/log")
    void logEvent(@RequestBody AuditEventRequest request);

    @lombok.extern.slf4j.Slf4j
    @org.springframework.stereotype.Component
    class Fallback implements AuditClient {
        @Override
        public void logEvent(AuditEventRequest request) {
            log.warn("Audit service unavailable — event dropped: action={}", request.getAction());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class AuditEventRequest {
        private UUID   userId;
        private UUID   societyId;
        private String action;
        private String entityType;
        private UUID   entityId;
        private String ipAddress;
        private String description;
    }
}