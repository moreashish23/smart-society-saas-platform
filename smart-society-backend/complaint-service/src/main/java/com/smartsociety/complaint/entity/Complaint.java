package com.smartsociety.complaint.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "complaints",
        indexes = {
                @Index(name = "idx_complaints_society_id",  columnList = "society_id"),
                @Index(name = "idx_complaints_status",       columnList = "society_id, status"),
                @Index(name = "idx_complaints_priority",     columnList = "society_id, priority"),
                @Index(name = "idx_complaints_sla_deadline", columnList = "sla_deadline"),
                @Index(name = "idx_complaints_escalation",   columnList = "escalation_level, status")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "society_id", nullable = false)
    private UUID societyId;

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(name = "assigned_to_id")
    private UUID assignedToId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;

    @Column(name = "escalation_level", nullable = false)
    @Builder.Default
    private Integer escalationLevel = 0;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "reopen_count", nullable = false)
    @Builder.Default
    private Integer reopenCount = 0;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(length = 255)
    private String location;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ComplaintTimeline> timeline = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ComplaintComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ComplaintAttachment> attachments = new ArrayList<>();


    public boolean isSlaBreached() {
        return LocalDateTime.now().isAfter(slaDeadline)
                && !ComplaintStatus.CLOSED.equals(status)
                && !ComplaintStatus.CANCELLED.equals(status);
    }


    public boolean canBeAssigned() {
        return ComplaintStatus.OPEN.equals(status)
                || ComplaintStatus.REOPENED.equals(status);
    }


    public boolean canBeResolved() {
        return ComplaintStatus.IN_PROGRESS.equals(status)
                || ComplaintStatus.ASSIGNED.equals(status);
    }


    public boolean canBeClosed() {
        return ComplaintStatus.PENDING_VERIFICATION.equals(status);
    }


    public boolean canBeReopened() {
        return ComplaintStatus.PENDING_VERIFICATION.equals(status);
    }


    public void escalate() {
        if (escalationLevel < 3) {
            escalationLevel++;
            escalatedAt = LocalDateTime.now();
        }
    }


    public void reopen() {
        status = ComplaintStatus.REOPENED;
        reopenCount++;
        resolvedAt = null;
    }
}