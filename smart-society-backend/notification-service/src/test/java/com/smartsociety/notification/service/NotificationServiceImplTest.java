package com.smartsociety.notification.service;

import com.smartsociety.notification.dto.request.SendNotificationRequest;
import com.smartsociety.notification.dto.response.NotificationResponse;
import com.smartsociety.notification.entity.Notification;
import com.smartsociety.notification.entity.NotificationType;
import com.smartsociety.notification.mapper.NotificationMapper;
import com.smartsociety.notification.repository.NotificationRepository;
import com.smartsociety.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Unit Tests")
class NotificationServiceImplTest {

    @Mock private NotificationRepository   notificationRepository;
    @Mock private NotificationMapper       notificationMapper;
    @Mock private SimpMessagingTemplate    messagingTemplate;

    @InjectMocks private NotificationServiceImpl notificationService;

    private UUID societyId;
    private UUID userId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        societyId      = UUID.randomUUID();
        userId         = UUID.randomUUID();
        notificationId = UUID.randomUUID();
    }

    @Nested @DisplayName("send()")
    class SendTests {

        @Test @DisplayName("Should persist notification and return response")
        void send_persistsBroadcast() {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .societyId(societyId).recipientId(null)
                    .type(NotificationType.COMPLAINT_CREATED)
                    .title("New Complaint").message("A new complaint has been raised")
                    .build();

            Notification saved = Notification.builder()
                    .id(notificationId).societyId(societyId)
                    .type(NotificationType.COMPLAINT_CREATED)
                    .title("New Complaint").message("A new complaint has been raised")
                    .build();

            NotificationResponse expected = new NotificationResponse();
            expected.setId(notificationId);

            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
            when(notificationMapper.toResponse(saved)).thenReturn(expected);

            NotificationResponse result = notificationService.send(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(notificationId);
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test @DisplayName("Should persist personal notification with recipientId")
        void send_personalNotification() {
            UUID recipientId = UUID.randomUUID();

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .societyId(societyId).recipientId(recipientId)
                    .type(NotificationType.COMPLAINT_ASSIGNED)
                    .title("Assigned").message("Complaint assigned to you")
                    .build();

            Notification saved = Notification.builder()
                    .id(notificationId).societyId(societyId).recipientId(recipientId)
                    .type(NotificationType.COMPLAINT_ASSIGNED)
                    .title("Assigned").message("Complaint assigned to you")
                    .build();

            NotificationResponse expected = new NotificationResponse();
            expected.setId(notificationId);
            expected.setRecipientId(recipientId);

            when(notificationRepository.save(any())).thenReturn(saved);
            when(notificationMapper.toResponse(saved)).thenReturn(expected);

            NotificationResponse result = notificationService.send(request);

            assertThat(result.getRecipientId()).isEqualTo(recipientId);
        }
    }

    @Nested @DisplayName("countUnread()")
    class CountTests {

        @Test @DisplayName("Should return unread count for user")
        void countUnread_returnsCorrectCount() {
            when(notificationRepository.countUnreadForUser(userId, societyId)).thenReturn(5L);

            long count = notificationService.countUnread(userId, societyId);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested @DisplayName("markAllRead()")
    class MarkAllReadTests {

        @Test @DisplayName("Should mark all unread notifications as read")
        void markAllRead_success() {
            when(notificationRepository.markAllReadForUser(userId, societyId)).thenReturn(3);

            int count = notificationService.markAllRead(userId, societyId);

            assertThat(count).isEqualTo(3);
            verify(notificationRepository).markAllReadForUser(userId, societyId);
        }
    }
}