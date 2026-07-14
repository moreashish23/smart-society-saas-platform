package com.smartsociety.notification.service.impl;

import com.smartsociety.notification.dto.request.SendNotificationRequest;
import com.smartsociety.notification.dto.response.NotificationResponse;
import com.smartsociety.notification.dto.response.PagedResponse;
import com.smartsociety.notification.entity.Notification;
import com.smartsociety.notification.exception.NotificationNotFoundException;
import com.smartsociety.notification.mapper.NotificationMapper;
import com.smartsociety.notification.repository.NotificationRepository;
import com.smartsociety.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository   notificationRepository;
    private final NotificationMapper       notificationMapper;
    private final SimpMessagingTemplate    messagingTemplate;


    @Override
    @Transactional
    public NotificationResponse send(SendNotificationRequest request) {
        // 1. Persist
        Notification notification = Notification.builder()
                .societyId(request.getSocietyId())
                .recipientId(request.getRecipientId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .entityId(request.getEntityId())
                .entityType(request.getEntityType())
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);

        // 2. Push over WebSocket (async — never blocks the caller)
        pushAsync(response, request.getRecipientId(), request.getSocietyId());

        log.info("Notification sent: type={}, societyId={}, recipientId={}",
                request.getType(), request.getSocietyId(), request.getRecipientId());

        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getForUser(UUID userId, UUID societyId,
                                                          boolean unreadOnly, Pageable pageable) {
        var page = unreadOnly
                ? notificationRepository.findUnreadForUser(userId, societyId, pageable)
                : notificationRepository.findForUser(userId, societyId, pageable);

        return PagedResponse.of(page.map(notificationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID userId, UUID societyId) {
        return notificationRepository.countUnreadForUser(userId, societyId);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID notificationId, UUID userId, UUID societyId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId.toString()));

        boolean isBroadcastForMySociety =
                notification.getRecipientId() == null
                        && Objects.equals(notification.getSocietyId(), societyId);
        boolean isMyPersonalNotification =
                Objects.equals(notification.getRecipientId(), userId);

        if (!isBroadcastForMySociety && !isMyPersonalNotification) {
            throw new NotificationNotFoundException(notificationId.toString());
        }

        notification.markRead();
        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public int markAllRead(UUID userId, UUID societyId) {
        int count = notificationRepository.markAllReadForUser(userId, societyId);
        log.info("Marked {} notifications as read for userId={}", count, userId);
        return count;
    }

    // ── WebSocket push ────────────────────────────────────────────────────────

    @Async
    void pushAsync(NotificationResponse payload, UUID recipientId, UUID societyId) {
        try {
            if (recipientId != null) {

                messagingTemplate.convertAndSendToUser(
                        recipientId.toString(),
                        "/queue/notifications",
                        payload);
                log.debug("WS push → user {}: {}", recipientId, payload.getType());
            } else {
                // Broadcast to everyone in the society
                String destination = "/topic/society/" + societyId;
                messagingTemplate.convertAndSend(destination, payload);
                log.debug("WS broadcast → society {}: {}", societyId, payload.getType());
            }
        } catch (Exception ex) {
            log.warn("WebSocket push failed: {}", ex.getMessage());
        }
    }
}