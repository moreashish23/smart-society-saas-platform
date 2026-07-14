package com.smartsociety.vendor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "vendors",
        indexes = {
                @Index(name = "idx_vendors_society_id", columnList = "society_id"),
                @Index(name = "idx_vendors_user_id",    columnList = "user_id"),
                @Index(name = "idx_vendors_status",      columnList = "society_id, status"),
                @Index(name = "idx_vendors_category",    columnList = "society_id, service_category"),
                @Index(name = "idx_vendors_rating",      columnList = "society_id, rating")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_vendor_user_society",
                columnNames = {"user_id", "society_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "society_id", nullable = false)
    private UUID societyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "contact_person", nullable = false, length = 100)
    private String contactPerson;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_category", nullable = false, length = 30)
    private ServiceCategory serviceCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String address;

    @Column(nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_jobs", nullable = false)
    @Builder.Default
    private Integer totalJobs = 0;

    @Column(name = "completed_jobs", nullable = false)
    @Builder.Default
    private Integer completedJobs = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VendorStatus status = VendorStatus.PENDING_APPROVAL;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Business methods ──────────────────────────────────────────────────────

    public void approve(UUID approverId) {
        this.status     = VendorStatus.ACTIVE;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
    }

    public void incrementJobs() {
        this.totalJobs = (this.totalJobs == null ? 0 : this.totalJobs) + 1;
    }

    public void incrementCompletedJobs() {
        this.completedJobs = (this.completedJobs == null ? 0 : this.completedJobs) + 1;
    }

    public boolean isActive() {
        return VendorStatus.ACTIVE.equals(this.status);
    }
}