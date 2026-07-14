package com.smartsociety.complaint.client;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@FeignClient(
        name = "notification-service",
        url  = "${app.services.notification-service-url}",
        fallback = NotificationClient.NotificationClientFallback.class
)
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationRequest request);

    @lombok.extern.slf4j.Slf4j
    @org.springframework.stereotype.Component
    class NotificationClientFallback implements NotificationClient {
        @Override
        public void sendNotification(NotificationRequest request) {
            log.warn("Notification service unavailable — event dropped: type={}, societyId={}",
                    request.getType(), request.getSocietyId());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class NotificationRequest {
        private UUID societyId;
        private UUID recipientId;
        private String type;
        private String title;
        private String message;
        private UUID entityId;
        private String entityType;
    }
}