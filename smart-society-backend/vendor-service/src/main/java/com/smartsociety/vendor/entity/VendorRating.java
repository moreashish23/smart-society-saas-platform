package com.smartsociety.vendor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "vendor_ratings",
        indexes = {
                @Index(name = "idx_vendor_ratings_vendor_id", columnList = "vendor_id"),
                @Index(name = "idx_vendor_ratings_society",   columnList = "society_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_vendor_rating_complaint",
                columnNames = {"vendor_id", "complaint_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorRating {

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

    @Column(name = "rated_by", nullable = false)
    private UUID ratedBy;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}