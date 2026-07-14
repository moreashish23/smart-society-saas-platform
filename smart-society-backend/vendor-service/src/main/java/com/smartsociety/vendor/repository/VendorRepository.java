package com.smartsociety.vendor.repository;

import com.smartsociety.vendor.entity.Vendor;
import com.smartsociety.vendor.entity.ServiceCategory;
import com.smartsociety.vendor.entity.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {


    boolean existsByUserIdAndSocietyId(UUID userId, UUID societyId);


    Optional<Vendor> findByUserIdAndSocietyId(UUID userId, UUID societyId);


    Optional<Vendor> findByIdAndSocietyId(UUID id, UUID societyId);


    Page<Vendor> findBySocietyIdAndStatus(UUID societyId, VendorStatus status, Pageable pageable);


    @Query("""
            SELECT v FROM Vendor v
            WHERE v.societyId = :societyId
              AND (:status   IS NULL OR v.status          = :status)
              AND (:category IS NULL OR v.serviceCategory = :category)
              AND (:keyword  IS NULL
                   OR LOWER(v.businessName)  LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                   OR LOWER(v.contactPerson) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
            """)
    Page<Vendor> searchVendors(
            @Param("societyId") UUID societyId,
            @Param("status")    VendorStatus status,
            @Param("category")  ServiceCategory category,
            @Param("keyword")   String keyword,
            Pageable pageable);


    @Query(
            value = "SELECT AVG(rating) FROM vendor_ratings WHERE vendor_id = :vendorId",
            nativeQuery = true
    )
    Optional<BigDecimal> calculateAverageRating(@Param("vendorId") UUID vendorId);
}