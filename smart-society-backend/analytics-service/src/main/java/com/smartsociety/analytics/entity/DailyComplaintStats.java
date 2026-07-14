package com.smartsociety.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "daily_complaint_stats",
        indexes = {
                @Index(name = "idx_daily_stats_society", columnList = "society_id, stat_date"),
                @Index(name = "idx_daily_stats_date",    columnList = "stat_date")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_daily_stats_society_date",
                columnNames = {"society_id", "stat_date"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyComplaintStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "society_id", nullable = false)
    private UUID societyId;


    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_complaints", nullable = false)
    @Builder.Default
    private Integer totalComplaints = 0;

    @Column(name = "open_complaints", nullable = false)
    @Builder.Default
    private Integer openComplaints = 0;

    @Column(name = "closed_complaints", nullable = false)
    @Builder.Default
    private Integer closedComplaints = 0;

    @Column(name = "reopened_complaints", nullable = false)
    @Builder.Default
    private Integer reopenedComplaints = 0;

    @Column(name = "critical_complaints", nullable = false)
    @Builder.Default
    private Integer criticalComplaints = 0;

    @Column(name = "high_complaints", nullable = false)
    @Builder.Default
    private Integer highComplaints = 0;

    @Column(name = "medium_complaints", nullable = false)
    @Builder.Default
    private Integer mediumComplaints = 0;

    @Column(name = "low_complaints", nullable = false)
    @Builder.Default
    private Integer lowComplaints = 0;

    @Column(name = "sla_breaches", nullable = false)
    @Builder.Default
    private Integer slaBreaches = 0;


    @Column(name = "avg_resolution_hours", precision = 10, scale = 2)
    private BigDecimal avgResolutionHours;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}