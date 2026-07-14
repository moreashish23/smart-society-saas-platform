package com.smartsociety.notification.dto.response;

import com.smartsociety.notification.entity.NotificationType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID societyId;
    private UUID recipientId;
    private NotificationType type;
    private String title;
    private String message;
    private UUID entityId;
    private String entityType;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}