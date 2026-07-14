package com.smartsociety.society.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "society_members",
        indexes = {
                @Index(name = "idx_society_members_society", columnList = "society_id"),
                @Index(name = "idx_society_members_user",    columnList = "user_id"),
                @Index(name = "idx_society_members_status",  columnList = "society_id, status")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_society_member",
                columnNames = {"society_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocietyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(name = "flat_number", length = 20)
    private String flatNumber;

    @Column(length = 20)
    private String block;

    @Column
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Business methods ──────────────────────────────────────────────────────

    public void deactivate() {
        this.status = MemberStatus.INACTIVE;
        this.leftAt = LocalDateTime.now();
    }
}