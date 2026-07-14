package com.smartsociety.vendor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "vendor_jobs",
        indexes = {
                @Index(name = "idx_vendor_jobs_vendor_id",    columnList = "vendor_id"),
                @Index(name = "idx_vendor_jobs_society_id",   columnList = "society_id"),
                @Index(name = "idx_vendor_jobs_complaint_id", columnList = "complaint_id"),
                @Index(name = "idx_vendor_jobs_status",       columnList = "vendor_id, status")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_vendor_complaint",
                columnNames = {"vendor_id", "complaint_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "society_id", nullable = false)
    private UUID societyId;

    @Column(name = "complaint_id", nullable = false)
    private UUID complaintId;


    @Column(name = "complaint_title", length = 255)
    private String complaintTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private JobStatus status = JobStatus.ASSIGNED;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}