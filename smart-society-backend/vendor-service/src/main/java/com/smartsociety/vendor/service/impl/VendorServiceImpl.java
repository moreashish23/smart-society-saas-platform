package com.smartsociety.vendor.service.impl;

import com.smartsociety.vendor.client.AuditClient;
import com.smartsociety.vendor.dto.request.*;
import com.smartsociety.vendor.dto.response.*;
import com.smartsociety.vendor.entity.*;
import com.smartsociety.vendor.exception.*;
import com.smartsociety.vendor.mapper.VendorMapper;
import com.smartsociety.vendor.repository.*;
import com.smartsociety.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorServiceImpl implements VendorService {

    private final VendorRepository       vendorRepository;
    private final VendorJobRepository    jobRepository;
    private final VendorRatingRepository ratingRepository;
    private final VendorMapper           vendorMapper;
    private final AuditClient            auditClient;

    // ── REGISTER ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VendorResponse registerVendor(CreateVendorRequest request, UUID societyId, UUID createdBy) {
        if (vendorRepository.existsByUserIdAndSocietyId(request.getUserId(), societyId)) {
            throw new VendorAlreadyExistsException();
        }

        Vendor vendor = vendorMapper.toEntity(request);
        vendor.setSocietyId(societyId);

        Vendor saved = vendorRepository.save(vendor);
        audit(createdBy, societyId, saved.getId(), "VENDOR_REGISTER",
                "Vendor registered: " + saved.getBusinessName());

        log.info("Vendor registered: id={}, societyId={}", saved.getId(), societyId);
        return vendorMapper.toResponse(saved);
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendor(UUID vendorId, UUID societyId) {
        return vendorMapper.toResponse(findInSociety(vendorId, societyId));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendorByUserId(UUID userId, UUID societyId) {
        Vendor vendor = vendorRepository.findByUserIdAndSocietyId(userId, societyId)
                .orElseThrow(() -> new VendorNotFoundException(userId.toString()));
        return vendorMapper.toResponse(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VendorResponse> getVendors(UUID societyId, VendorStatus status,
                                                    ServiceCategory category, String keyword,
                                                    Pageable pageable) {
        return PagedResponse.of(vendorRepository
                .searchVendors(societyId, status, category, keyword, pageable)
                .map(vendorMapper::toResponse));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VendorResponse updateVendor(UUID vendorId, UpdateVendorRequest request,
                                       UUID requesterId, UUID societyId) {
        Vendor vendor = findInSociety(vendorId, societyId);
        vendorMapper.updateFromRequest(request, vendor);
        Vendor updated = vendorRepository.save(vendor);

        audit(requesterId, societyId, vendorId, "VENDOR_UPDATE",
                "Vendor updated: " + updated.getBusinessName());
        return vendorMapper.toResponse(updated);
    }

    // ── APPROVE / SUSPEND / ACTIVATE ─────────────────────────────────────────

    @Override
    @Transactional
    public VendorResponse approveVendor(UUID vendorId, UUID approverId, UUID societyId) {
        Vendor vendor = findInSociety(vendorId, societyId);
        vendor.approve(approverId);
        Vendor updated = vendorRepository.save(vendor);

        audit(approverId, societyId, vendorId, "VENDOR_APPROVE",
                "Vendor approved: " + updated.getBusinessName());

        log.info("Vendor approved: id={}", vendorId);
        return vendorMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public VendorResponse suspendVendor(UUID vendorId, UUID requesterId, UUID societyId) {
        Vendor vendor = findInSociety(vendorId, societyId);
        vendor.setStatus(VendorStatus.SUSPENDED);
        Vendor updated = vendorRepository.save(vendor);

        audit(requesterId, societyId, vendorId, "VENDOR_SUSPEND",
                "Vendor suspended: " + updated.getBusinessName());
        return vendorMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public VendorResponse activateVendor(UUID vendorId, UUID requesterId, UUID societyId) {
        Vendor vendor = findInSociety(vendorId, societyId);
        vendor.setStatus(VendorStatus.ACTIVE);
        Vendor updated = vendorRepository.save(vendor);

        audit(requesterId, societyId, vendorId, "VENDOR_ACTIVATE",
                "Vendor activated: " + updated.getBusinessName());
        return vendorMapper.toResponse(updated);
    }

    // ── JOB TRACKING ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VendorJobResponse recordJob(UUID vendorId, RecordJobRequest request,
                                       UUID societyId, UUID requesterId) {
        Vendor vendor = findInSociety(vendorId, societyId);

        // Idempotent — if job already exists for this complaint, return it
        if (jobRepository.existsByVendorIdAndComplaintId(vendorId, request.getComplaintId())) {
            return jobRepository.findByVendorIdAndComplaintId(vendorId, request.getComplaintId())
                    .map(vendorMapper::toJobResponse)
                    .orElseThrow(JobNotFoundException::new);
        }

        VendorJob job = VendorJob.builder()
                .vendor(vendor)
                .societyId(societyId)
                .complaintId(request.getComplaintId())
                .complaintTitle(request.getComplaintTitle())
                .notes(request.getNotes())
                .status(JobStatus.ASSIGNED)
                .build();

        vendor.incrementJobs();
        vendorRepository.save(vendor);

        VendorJob saved = jobRepository.save(job);
        log.info("Job recorded: vendorId={}, complaintId={}", vendorId, request.getComplaintId());
        return vendorMapper.toJobResponse(saved);
    }

    @Override
    @Transactional
    public VendorJobResponse createJobForUser(CreateJobByUserRequest request, UUID societyId,
                                              UUID requesterId) {
        // complaint-service only knows the assignee's USER id (Complaint.assignedToId),
        // not vendor-service's internal Vendor.id — resolve it here, then delegate
        // to the existing idempotent recordJob() rather than duplicating its logic.
        Vendor vendor = vendorRepository.findByUserIdAndSocietyId(request.getUserId(), societyId)
                .orElseThrow(() -> new VendorNotFoundException(request.getUserId().toString()));

        RecordJobRequest recordJobRequest = RecordJobRequest.builder()
                .complaintId(request.getComplaintId())
                .complaintTitle(request.getComplaintTitle())
                .notes(request.getNotes())
                .build();

        return recordJob(vendor.getId(), recordJobRequest, societyId, requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VendorJobResponse> getVendorJobs(UUID vendorId, UUID societyId,
                                                          Pageable pageable) {
        findInSociety(vendorId, societyId); // access check
        return PagedResponse.of(jobRepository.findByVendorId(vendorId, pageable)
                .map(vendorMapper::toJobResponse));
    }

    @Override
    @Transactional
    public VendorJobResponse updateJobStatus(UUID vendorId, UUID jobId, JobStatus newStatus,
                                             UUID requesterId, UUID societyId) {
        findInSociety(vendorId, societyId);

        VendorJob job = jobRepository.findById(jobId)
                .filter(j -> j.getVendor().getId().equals(vendorId))
                .orElseThrow(JobNotFoundException::new);

        job.setStatus(newStatus);

        switch (newStatus) {
            case ACCEPTED   -> job.setAcceptedAt(LocalDateTime.now());
            case IN_PROGRESS-> job.setStartedAt(LocalDateTime.now());
            case COMPLETED  -> {
                job.setCompletedAt(LocalDateTime.now());
                Vendor vendor = job.getVendor();
                vendor.incrementCompletedJobs();
                vendorRepository.save(vendor);
            }
            case CANCELLED  -> job.setCancelledAt(LocalDateTime.now());
            default -> { /* no-op for ASSIGNED */ }
        }

        VendorJob updated = jobRepository.save(job);
        log.info("Job status updated: jobId={}, status={}", jobId, newStatus);
        return vendorMapper.toJobResponse(updated);
    }

    // ── RATINGS ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VendorRatingResponse rateVendor(UUID vendorId, RateVendorRequest request,
                                           UUID ratedBy, UUID societyId) {
        Vendor vendor = findInSociety(vendorId, societyId);

        if (ratingRepository.existsByVendorIdAndComplaintId(vendorId, request.getComplaintId())) {
            throw new DuplicateRatingException();
        }

        VendorRating rating = VendorRating.builder()
                .vendor(vendor)
                .societyId(societyId)
                .complaintId(request.getComplaintId())
                .ratedBy(ratedBy)
                .rating(request.getRating())
                .review(request.getReview())
                .build();

        ratingRepository.save(rating);

        // Recompute aggregate rating
        vendorRepository.calculateAverageRating(vendorId).ifPresent(avg -> {
            vendor.setRating(avg.setScale(2, RoundingMode.HALF_UP));
            vendorRepository.save(vendor);
        });

        audit(ratedBy, societyId, vendorId, "VENDOR_RATED",
                "Vendor rated " + request.getRating() + "/5 for complaint " + request.getComplaintId());

        log.info("Vendor rated: vendorId={}, rating={}", vendorId, request.getRating());
        return vendorMapper.toRatingResponse(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VendorRatingResponse> getVendorRatings(UUID vendorId, UUID societyId,
                                                                Pageable pageable) {
        findInSociety(vendorId, societyId);
        return PagedResponse.of(ratingRepository.findByVendorId(vendorId, pageable)
                .map(vendorMapper::toRatingResponse));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Vendor findInSociety(UUID vendorId, UUID societyId) {
        return vendorRepository.findByIdAndSocietyId(vendorId, societyId)
                .orElseThrow(() -> new VendorNotFoundException(vendorId.toString()));
    }

    @Async
    void audit(UUID userId, UUID societyId, UUID entityId, String action, String description) {
        try {
            auditClient.logEvent(AuditClient.AuditEventRequest.builder()
                    .userId(userId).societyId(societyId).action(action)
                    .entityType("VENDOR").entityId(entityId).description(description).build());
        } catch (Exception ex) { log.warn("Audit failed: {}", ex.getMessage()); }
    }
}