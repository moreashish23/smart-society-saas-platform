package com.smartsociety.notification.controller;

import com.smartsociety.notification.dto.request.SendNotificationRequest;
import com.smartsociety.notification.dto.response.ApiResponse;
import com.smartsociety.notification.dto.response.NotificationResponse;
import com.smartsociety.notification.dto.response.PagedResponse;
import com.smartsociety.notification.security.RequestContext;
import com.smartsociety.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Send notifications and manage notification history")
public class NotificationController {

    private final NotificationService notificationService;
    private final RequestContext      requestContext;

    @PostMapping("/send")
    @Operation(summary = "Send a notification [Internal — called by other services]")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @Valid @RequestBody SendNotificationRequest request) {

        NotificationResponse response = notificationService.send(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification sent"));
    }


    @GetMapping
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId    = requestContext.getUserId();
        UUID societyId = requestContext.getSocietyId();

        PagedResponse<NotificationResponse> result = notificationService.getForUser(
                userId, societyId, unreadOnly,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return ResponseEntity.ok(ApiResponse.success(result));
    }


    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        long count = notificationService.countUnread(
                requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }


    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @PathVariable UUID notificationId) {

        NotificationResponse response = notificationService.markRead(
                notificationId, requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(response, "Marked as read"));
    }


    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead() {
        int count = notificationService.markAllRead(
                requestContext.getUserId(), requestContext.getSocietyId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("markedRead", count),
                count + " notifications marked as read"));
    }
}