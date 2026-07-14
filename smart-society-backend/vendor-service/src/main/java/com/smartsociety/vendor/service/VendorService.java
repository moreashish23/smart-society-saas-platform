package com.smartsociety.vendor.service;

import com.smartsociety.vendor.dto.request.*;
import com.smartsociety.vendor.dto.response.*;
import com.smartsociety.vendor.entity.*;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface VendorService {
    VendorResponse                 registerVendor(CreateVendorRequest request, UUID societyId, UUID createdBy);
    VendorResponse                 getVendor(UUID vendorId, UUID societyId);
    VendorResponse                 getVendorByUserId(UUID userId, UUID societyId);
    PagedResponse<VendorResponse>  getVendors(UUID societyId, VendorStatus status, ServiceCategory category, String keyword, Pageable pageable);
    VendorResponse                 updateVendor(UUID vendorId, UpdateVendorRequest request, UUID requesterId, UUID societyId);
    VendorResponse                 approveVendor(UUID vendorId, UUID approverId, UUID societyId);
    VendorResponse                 suspendVendor(UUID vendorId, UUID requesterId, UUID societyId);
    VendorResponse                 activateVendor(UUID vendorId, UUID requesterId, UUID societyId);
    VendorJobResponse              recordJob(UUID vendorId, RecordJobRequest request, UUID societyId, UUID requesterId);
    VendorJobResponse              createJobForUser(CreateJobByUserRequest request, UUID societyId, UUID requesterId);
    PagedResponse<VendorJobResponse> getVendorJobs(UUID vendorId, UUID societyId, Pageable pageable);
    VendorJobResponse              updateJobStatus(UUID vendorId, UUID jobId, JobStatus newStatus, UUID requesterId, UUID societyId);
    VendorRatingResponse           rateVendor(UUID vendorId, RateVendorRequest request, UUID ratedBy, UUID societyId);
    PagedResponse<VendorRatingResponse> getVendorRatings(UUID vendorId, UUID societyId, Pageable pageable);
}