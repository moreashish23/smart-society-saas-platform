package com.smartsociety.complaint.service.impl;

import com.smartsociety.complaint.client.AuditClient;
import com.smartsociety.complaint.client.NotificationClient;
import com.smartsociety.complaint.client.VendorClient;
import com.smartsociety.complaint.dto.request.*;
import com.smartsociety.complaint.dto.response.*;
import com.smartsociety.complaint.entity.*;
import com.smartsociety.complaint.exception.*;
import com.smartsociety.complaint.mapper.ComplaintMapper;
import com.smartsociety.complaint.repository.*;
import com.smartsociety.complaint.service.ComplaintService;
import com.smartsociety.complaint.util.SlaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository           complaintRepository;
    private final ComplaintTimelineRepository   timelineRepository;
    private final ComplaintCommentRepository    commentRepository;
    private final ComplaintMapper               complaintMapper;
    private final SlaProperties                 slaProperties;
    private final NotificationClient            notificationClient;
    private final AuditClient                   auditClient;
    private final VendorClient                  vendorClient;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request,
                                             UUID residentId, UUID societyId) {
        ComplaintPriority priority = request.getPriority() != null
                ? request.getPriority() : ComplaintPriority.MEDIUM;

        Complaint complaint = Complaint.builder()
                .societyId(societyId)
                .residentId(residentId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(priority)
                .status(ComplaintStatus.OPEN)
                .location(request.getLocation())
                .slaDeadline(slaProperties.computeDeadline(priority))
                .build();

        Complaint saved = complaintRepository.save(complaint);
        addTimeline(saved, TimelineAction.CREATED, residentId, "Complaint created by resident");

        // Notify all managers in the society
        notifyAsync(NotificationClient.NotificationRequest.builder()
                .societyId(societyId)
                .type("COMPLAINT_CREATED")
                .title("New Complaint: " + saved.getTitle())
                .message("Priority: " + priority + " | Category: " + saved.getCategory())
                .entityId(saved.getId()).entityType("COMPLAINT")
                .build());

        auditAsync(residentId, societyId, saved.getId(), "COMPLAINT_CREATE",
                "Complaint created: " + saved.getTitle());

        log.info("Complaint created: id={}, societyId={}, priority={}", saved.getId(), societyId, priority);
        return buildFullResponse(saved);
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaint(UUID complaintId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);
        return buildFullResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> getComplaints(UUID societyId, ComplaintStatus status,
                                                          ComplaintCategory category,
                                                          ComplaintPriority priority,
                                                          String keyword, Pageable pageable) {
        Page<ComplaintResponse> page = complaintRepository
                .searchComplaints(societyId, status, category, priority, keyword, pageable)
                .map(this::buildFullResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> getMyComplaints(UUID residentId, UUID societyId,
                                                            Pageable pageable) {
        Page<ComplaintResponse> page = complaintRepository
                .findBySocietyIdAndResidentId(societyId, residentId, pageable)
                .map(this::buildFullResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> getAssignedComplaints(UUID assignedToId, Pageable pageable) {
        Page<ComplaintResponse> page = complaintRepository
                .findByAssignedToId(assignedToId, pageable)
                .map(this::buildFullResponse);
        return PagedResponse.of(page);
    }

    // ── ASSIGN ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ComplaintResponse assignComplaint(UUID complaintId, AssignComplaintRequest request,
                                             UUID managerId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);

        if (!complaint.canBeAssigned()) {
            throw new InvalidStatusTransitionException(complaint.getStatus().name(), "ASSIGNED");
        }

        complaint.setAssignedToId(request.getAssignedToId());
        complaint.setAssignedAt(LocalDateTime.now());
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaintRepository.save(complaint);

        addTimeline(complaint, TimelineAction.ASSIGNED, managerId,
                request.getNote() != null ? request.getNote() : "Assigned to vendor/staff");


        createVendorJob(complaint);

        notifyAsync(NotificationClient.NotificationRequest.builder()
                .societyId(societyId).recipientId(request.getAssignedToId())
                .type("COMPLAINT_ASSIGNED")
                .title("Complaint Assigned to You")
                .message("You have been assigned complaint: " + complaint.getTitle())
                .entityId(complaintId).entityType("COMPLAINT").build());

        // Also notify resident
        notifyAsync(NotificationClient.NotificationRequest.builder()
                .societyId(societyId).recipientId(complaint.getResidentId())
                .type("COMPLAINT_ASSIGNED")
                .title("Your Complaint Is Being Handled")
                .message("Your complaint has been assigned to our team")
                .entityId(complaintId).entityType("COMPLAINT").build());

        auditAsync(managerId, societyId, complaintId, "COMPLAINT_ASSIGN",
                "Assigned to: " + request.getAssignedToId());

        log.info("Complaint assigned: id={}, assignedTo={}", complaintId, request.getAssignedToId());
        return buildFullResponse(complaint);
    }

    // ── VENDOR WORKFLOW ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ComplaintResponse acceptComplaint(UUID complaintId, UUID vendorId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);
        validateAssignedTo(complaint, vendorId);

        if (!ComplaintStatus.ASSIGNED.equals(complaint.getStatus())) {
            throw new InvalidStatusTransitionException(complaint.getStatus().name(), "IN_PROGRESS");
        }

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.save(complaint);
        addTimeline(complaint, TimelineAction.ACCEPTED, vendorId, "Vendor accepted the complaint");

        notifyResident(complaint, "COMPLAINT_ACCEPTED",
                "Complaint Accepted", "Work will begin shortly on your complaint");

        log.info("Complaint accepted: id={}, vendorId={}", complaintId, vendorId);
        return buildFullResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponse startWork(UUID complaintId, UUID vendorId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);
        validateAssignedTo(complaint, vendorId);

        if (!ComplaintStatus.IN_PROGRESS.equals(complaint.getStatus())) {
            throw new InvalidStatusTransitionException(complaint.getStatus().name(), "IN_PROGRESS");
        }

        complaintRepository.save(complaint);
        addTimeline(complaint, TimelineAction.WORK_STARTED, vendorId, "Work has started");

        notifyResident(complaint, "COMPLAINT_WORK_STARTED",
                "Work Started", "Our team has started working on your complaint");

        log.info("Work started: complaintId={}", complaintId);
        return buildFullResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponse markWorkCompleted(UUID complaintId, UUID vendorId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);
        validateAssignedTo(complaint, vendorId);

        if (!ComplaintStatus.IN_PROGRESS.equals(complaint.getStatus())) {
            throw new InvalidStatusTransitionException(complaint.getStatus().name(), "WORK_COMPLETED");
        }

        complaintRepository.save(complaint);
        addTimeline(complaint, TimelineAction.WORK_COMPLETED, vendorId, "Vendor has completed work");

        log.info("Work completed by vendor: complaintId={}", complaintId);
        return buildFullResponse(complaint);
    }

    // ── RESOLVE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ComplaintResponse markResolved(UUID complaintId, ResolveComplaintRequest request,
                                          UUID managerId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);

        if (!complaint.canBeResolved()) {
            throw new InvalidStatusTransitionException(complaint.getStatus().name(), "PENDING_VERIFICATION");
        }

        complaint.setStatus(ComplaintStatus.PENDING_VERIFICATION);
        complaint.setResolutionNote(request.getResolutionNote());
        complaint.setResolvedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        addTimeline(complaint, TimelineAction.MARKED_RESOLVED, managerId,
                "Manager marked as resolved: " + request.getResolutionNote());

        // Critical: notify resident for verification
        notifyAsync(NotificationClient.NotificationRequest.builder()
                .societyId(societyId).recipientId(complaint.getResidentId())
                .type("COMPLAINT_PENDING_VERIFICATION")
                .title("Has Your Issue Been Resolved?")
                .message("Please confirm if your complaint has been resolved. Tap YES to close or NO to reopen.")
                .entityId(complaintId).entityType("COMPLAINT").build());

        auditAsync(managerId, societyId, complaintId, "COMPLAINT_RESOLVE",
                "Marked resolved by manager");

        log.info("Complaint marked resolved: id={}, pending verification", complaintId);
        return buildFullResponse(complaint);
    }

    // ── RESIDENT VERIFICATION ─────────────────────────────────────────────────

    @Override
    @Transactional
    public ComplaintResponse verifyResolution(UUID complaintId, boolean resolved,
                                              UUID residentId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);

        if (!complaint.canBeClosed() && !complaint.canBeReopened()) {
            throw new InvalidStatusTransitionException(
                    complaint.getStatus().name(), resolved ? "CLOSED" : "REOPENED");
        }

        // Verify this is the complaint owner
        if (!complaint.getResidentId().equals(residentId)) {
            throw new AccessDeniedException();
        }

        if (resolved) {
            // Happy path: resident confirms resolution
            complaint.setStatus(ComplaintStatus.CLOSED);
            complaint.setClosedAt(LocalDateTime.now());
            complaintRepository.save(complaint);

            addTimeline(complaint, TimelineAction.VERIFIED_RESOLVED, residentId,
                    "Resident confirmed complaint resolved");

            auditAsync(residentId, societyId, complaintId, "COMPLAINT_CLOSE",
                    "Closed by resident verification");

            log.info("Complaint CLOSED by resident: id={}", complaintId);

        } else {
            // Resident rejects — reopen
            complaint.reopen();
            complaintRepository.save(complaint);

            addTimeline(complaint, TimelineAction.REOPENED, residentId,
                    "Resident rejected resolution — complaint reopened");

            // Notify manager immediately
            notifyAsync(NotificationClient.NotificationRequest.builder()
                    .societyId(societyId)
                    .type("COMPLAINT_REOPENED")
                    .title("Complaint Reopened")
                    .message("Resident rejected the resolution for: " + complaint.getTitle()
                            + " (Reopen #" + complaint.getReopenCount() + ")")
                    .entityId(complaintId).entityType("COMPLAINT").build());

            auditAsync(residentId, societyId, complaintId, "COMPLAINT_REOPEN",
                    "Reopened by resident — reopen count: " + complaint.getReopenCount());

            log.info("Complaint REOPENED by resident: id={}, reopenCount={}", complaintId, complaint.getReopenCount());
        }

        return buildFullResponse(complaint);
    }

    // ── CANCEL ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ComplaintResponse cancelComplaint(UUID complaintId, UUID requesterId, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);

        if (ComplaintStatus.CLOSED.equals(complaint.getStatus()) ||
                ComplaintStatus.CANCELLED.equals(complaint.getStatus())) {
            throw new InvalidStatusTransitionException(complaint.getStatus().name(), "CANCELLED");
        }

        complaint.setStatus(ComplaintStatus.CANCELLED);
        complaintRepository.save(complaint);

        addTimeline(complaint, TimelineAction.CANCELLED, requesterId, "Complaint cancelled");

        auditAsync(requesterId, societyId, complaintId, "COMPLAINT_CANCEL", "Complaint cancelled");

        log.info("Complaint cancelled: id={}", complaintId);
        return buildFullResponse(complaint);
    }

    // ── COMMENTS ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CommentResponse addComment(UUID complaintId, AddCommentRequest request,
                                      UUID authorId, String authorRole, UUID societyId) {
        Complaint complaint = findInSociety(complaintId, societyId);

        ComplaintComment comment = ComplaintComment.builder()
                .complaint(complaint)
                .authorId(authorId)
                .authorRole(authorRole)
                .content(request.getContent())
                .build();

        ComplaintComment saved = commentRepository.save(comment);
        addTimeline(complaint, TimelineAction.COMMENTED, authorId, "Comment added");

        log.info("Comment added to complaint: complaintId={}", complaintId);
        return complaintMapper.toCommentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID complaintId, UUID societyId) {
        findInSociety(complaintId, societyId); // validate access
        return commentRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId)
                .stream().map(complaintMapper::toCommentResponse).collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Complaint findInSociety(UUID complaintId, UUID societyId) {
        return complaintRepository.findByIdAndSocietyId(complaintId, societyId)
                .orElseThrow(() -> new ComplaintNotFoundException(complaintId.toString()));
    }

    private void validateAssignedTo(Complaint complaint, UUID vendorId) {
        if (!vendorId.equals(complaint.getAssignedToId())) {
            throw new AccessDeniedException();
        }
    }

    private void addTimeline(Complaint complaint, TimelineAction action,
                             UUID performedBy, String note) {
        ComplaintTimeline entry = ComplaintTimeline.builder()
                .complaint(complaint).action(action)
                .performedBy(performedBy).note(note)
                .build();
        timelineRepository.save(entry);
    }

    private ComplaintResponse buildFullResponse(Complaint complaint) {
        ComplaintResponse response = complaintMapper.toResponse(complaint);

        response.setTimeline(timelineRepository
                .findByComplaintIdOrderByCreatedAtAsc(complaint.getId())
                .stream().map(complaintMapper::toTimelineResponse).collect(Collectors.toList()));

        response.setComments(commentRepository
                .findByComplaintIdOrderByCreatedAtAsc(complaint.getId())
                .stream().map(complaintMapper::toCommentResponse).collect(Collectors.toList()));

        return response;
    }

    private void createVendorJob(Complaint complaint) {
        try {
            vendorClient.createJob(VendorClient.CreateJobRequest.builder()
                    .userId(complaint.getAssignedToId())
                    .complaintId(complaint.getId())
                    .complaintTitle(complaint.getTitle())
                    .notes("Auto-created from complaint assignment")
                    .build());
            log.info("Vendor job created: userId={}, complaintId={}",
                    complaint.getAssignedToId(), complaint.getId());
        } catch (feign.FeignException.NotFound ex) {
            // Expected/normal: the assignee is internal staff (e.g.
            // MAINTENANCE_STAFF), not a registered vendor — there is no
            // vendor_jobs record to create for them, and that's fine.
            log.debug("Assignee {} is not a registered vendor — skipping job creation "
                    + "for complaintId={}", complaint.getAssignedToId(), complaint.getId());
        } catch (Exception ex) {
            // vendor-service unreachable, timing out, or erroring — don't
            // fail the whole assignment over it (same non-blocking
            // philosophy as notifyAsync/auditAsync below), but this is
            // more consequential than a dropped notification, so it's
            // logged at ERROR rather than WARN.
            log.error("Failed to create vendor job: userId={}, complaintId={}: {}",
                    complaint.getAssignedToId(), complaint.getId(), ex.getMessage());
        }
    }

    private void notifyResident(Complaint complaint, String type, String title, String message) {
        notifyAsync(NotificationClient.NotificationRequest.builder()
                .societyId(complaint.getSocietyId())
                .recipientId(complaint.getResidentId())
                .type(type).title(title).message(message)
                .entityId(complaint.getId()).entityType("COMPLAINT").build());
    }

    @Async
    void notifyAsync(NotificationClient.NotificationRequest request) {
        try { notificationClient.sendNotification(request); }
        catch (Exception ex) { log.warn("Notification failed: {}", ex.getMessage()); }
    }

    @Async
    void auditAsync(UUID userId, UUID societyId, UUID entityId, String action, String description) {
        try {
            auditClient.logEvent(AuditClient.AuditEventRequest.builder()
                    .userId(userId).societyId(societyId).action(action)
                    .entityType("COMPLAINT").entityId(entityId).description(description).build());
        } catch (Exception ex) { log.warn("Audit failed: {}", ex.getMessage()); }
    }
}