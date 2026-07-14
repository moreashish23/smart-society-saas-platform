package com.smartsociety.society.client;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@FeignClient(
        name     = "notification-service-from-society",
        url      = "${app.services.notification-service-url}",
        fallback = NotificationClient.Fallback.class
)
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationRequest request);

    @lombok.extern.slf4j.Slf4j
    @org.springframework.stereotype.Component
    class Fallback implements NotificationClient {
        @Override
        public void sendNotification(NotificationRequest request) {
            log.warn("Notification service unavailable — notice event dropped: type={}", request.getType());
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class NotificationRequest {
        private UUID   societyId;
        private UUID   recipientId;   // null = broadcast
        private String type;
        private String title;
        private String message;
        private UUID   entityId;
        private String entityType;
    }
}