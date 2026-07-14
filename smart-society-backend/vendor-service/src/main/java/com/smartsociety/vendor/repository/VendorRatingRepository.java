package com.smartsociety.vendor.repository;

import com.smartsociety.vendor.entity.VendorRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VendorRatingRepository extends JpaRepository<VendorRating, UUID> {


    boolean existsByVendorIdAndComplaintId(UUID vendorId, UUID complaintId);


    Page<VendorRating> findByVendorId(UUID vendorId, Pageable pageable);
}