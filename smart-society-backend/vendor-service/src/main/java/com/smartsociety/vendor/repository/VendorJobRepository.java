package com.smartsociety.vendor.repository;

import com.smartsociety.vendor.entity.VendorJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorJobRepository extends JpaRepository<VendorJob, UUID> {


    boolean existsByVendorIdAndComplaintId(UUID vendorId, UUID complaintId);


    Optional<VendorJob> findByVendorIdAndComplaintId(UUID vendorId, UUID complaintId);


    Page<VendorJob> findByVendorId(UUID vendorId, Pageable pageable);
}