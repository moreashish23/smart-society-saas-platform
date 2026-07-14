package com.smartsociety.notification.service;

import com.smartsociety.notification.dto.request.SendNotificationRequest;
import com.smartsociety.notification.dto.response.NotificationResponse;
import com.smartsociety.notification.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse send(SendNotificationRequest request);

    PagedResponse<NotificationResponse> getForUser(UUID userId, UUID societyId,
                                                   boolean unreadOnly, Pageable pageable);

    long countUnread(UUID userId, UUID societyId);

    NotificationResponse markRead(UUID notificationId, UUID userId, UUID societyId);

    int markAllRead(UUID userId, UUID societyId);
}