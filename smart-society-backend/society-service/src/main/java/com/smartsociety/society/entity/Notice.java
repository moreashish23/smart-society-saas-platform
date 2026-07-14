package com.smartsociety.society.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notices",
        indexes = {
                @Index(name = "idx_notices_society", columnList = "society_id"),
                @Index(name = "idx_notices_status",  columnList = "society_id, status"),
                @Index(name = "idx_notices_type",    columnList = "notice_type")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 30)
    @Builder.Default
    private NoticeType noticeType = NoticeType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private NoticeStatus status = NoticeStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private Boolean priority = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Business methods ──────────────────────────────────────────────────────

    public void publish() {
        this.status = NoticeStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = NoticeStatus.ARCHIVED;
    }

    public boolean isActive() {
        return NoticeStatus.PUBLISHED.equals(status)
                && (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
    }
}