package com.smartsociety.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "vendor_performance_stats",
        indexes = {
                @Index(name = "idx_vendor_perf_society", columnList = "society_id, stat_month"),
                @Index(name = "idx_vendor_perf_vendor",  columnList = "vendor_id, stat_month")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_vendor_perf_month",
                columnNames = {"vendor_id", "stat_month"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPerformanceStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "society_id", nullable = false)
    private UUID societyId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;


    @Column(name = "stat_month", nullable = false)
    private LocalDate statMonth;

    @Column(name = "jobs_assigned", nullable = false)
    @Builder.Default
    private Integer jobsAssigned = 0;

    @Column(name = "jobs_completed", nullable = false)
    @Builder.Default
    private Integer jobsCompleted = 0;

    @Column(name = "jobs_cancelled", nullable = false)
    @Builder.Default
    private Integer jobsCancelled = 0;


    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}