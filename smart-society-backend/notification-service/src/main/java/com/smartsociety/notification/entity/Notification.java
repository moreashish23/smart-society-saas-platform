package com.smartsociety.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient", columnList = "recipient_id, is_read"),
                @Index(name = "idx_notifications_society",   columnList = "society_id, created_at"),
                @Index(name = "idx_notifications_created_at",columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "society_id", nullable = false)
    private UUID societyId;


    @Column(name = "recipient_id")
    private UUID recipientId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "entity_type", length = 50)
    private String entityType;


    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;


    @Column(name = "read_at", insertable = false, updatable = false)
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── Business methods ─────────────────────────────────────────────────────

    public void markRead() {
        this.read = true;
        // readAt is NOT set here — PostgreSQL trigger handles it on UPDATE.
    }
}