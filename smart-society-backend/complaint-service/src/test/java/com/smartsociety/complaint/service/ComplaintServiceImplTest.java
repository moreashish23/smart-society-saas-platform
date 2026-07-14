package com.smartsociety.complaint.service;

import com.smartsociety.complaint.client.AuditClient;
import com.smartsociety.complaint.client.NotificationClient;
import com.smartsociety.complaint.dto.request.*;
import com.smartsociety.complaint.dto.response.ComplaintResponse;
import com.smartsociety.complaint.entity.*;
import com.smartsociety.complaint.exception.*;
import com.smartsociety.complaint.mapper.ComplaintMapper;
import com.smartsociety.complaint.repository.*;
import com.smartsociety.complaint.service.impl.ComplaintServiceImpl;
import com.smartsociety.complaint.util.SlaProperties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplaintServiceImpl Unit Tests")
class ComplaintServiceImplTest {

    @Mock private ComplaintRepository         complaintRepository;
    @Mock private ComplaintTimelineRepository timelineRepository;
    @Mock private ComplaintCommentRepository  commentRepository;
    @Mock private ComplaintMapper             complaintMapper;
    @Mock private SlaProperties               slaProperties;
    @Mock private NotificationClient          notificationClient;
    @Mock private AuditClient                 auditClient;

    @InjectMocks private ComplaintServiceImpl complaintService;

    private UUID societyId;
    private UUID residentId;
    private UUID managerId;
    private UUID vendorId;
    private UUID complaintId;
    private Complaint openComplaint;

    @BeforeEach
    void setUp() {
        societyId   = UUID.randomUUID();
        residentId  = UUID.randomUUID();
        managerId   = UUID.randomUUID();
        vendorId    = UUID.randomUUID();
        complaintId = UUID.randomUUID();

        openComplaint = Complaint.builder()
                .id(complaintId).societyId(societyId).residentId(residentId)
                .title("Water leakage in bathroom").description("Water is leaking from the ceiling since two days")
                .category(ComplaintCategory.WATER_LEAKAGE).priority(ComplaintPriority.HIGH)
                .status(ComplaintStatus.OPEN)
                .slaDeadline(LocalDateTime.now().plusHours(24))
                .escalationLevel(0).reopenCount(0)
                .build();
    }

    // ── createComplaint ───────────────────────────────────────────────────────

    @Nested @DisplayName("createComplaint()")
    class CreateTests {

        @Test @DisplayName("Should create complaint with computed SLA deadline")
        void create_success() {
            CreateComplaintRequest request = CreateComplaintRequest.builder()
                    .title("Water leakage").description("Water is leaking from the ceiling in bathroom")
                    .category(ComplaintCategory.WATER_LEAKAGE).priority(ComplaintPriority.HIGH).build();

            LocalDateTime deadline = LocalDateTime.now().plusHours(24);
            ComplaintResponse expected = new ComplaintResponse();

            when(slaProperties.computeDeadline(ComplaintPriority.HIGH)).thenReturn(deadline);
            when(complaintRepository.save(any())).thenReturn(openComplaint);
            when(timelineRepository.save(any())).thenReturn(new ComplaintTimeline());
            when(complaintMapper.toResponse(any())).thenReturn(expected);
            when(timelineRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
            when(commentRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

            ComplaintResponse result = complaintService.createComplaint(request, residentId, societyId);

            assertThat(result).isNotNull();
            verify(complaintRepository).save(any(Complaint.class));
            verify(timelineRepository).save(argThat(t -> TimelineAction.CREATED.equals(t.getAction())));
        }

        @Test @DisplayName("Should default priority to MEDIUM when not specified")
        void create_defaultPriority() {
            CreateComplaintRequest request = CreateComplaintRequest.builder()
                    .title("Noise complaint").description("Neighbour is playing loud music late at night")
                    .category(ComplaintCategory.NOISE_COMPLAINT).priority(null).build();

            when(slaProperties.computeDeadline(ComplaintPriority.MEDIUM))
                    .thenReturn(LocalDateTime.now().plusHours(48));
            when(complaintRepository.save(any())).thenReturn(openComplaint);
            when(timelineRepository.save(any())).thenReturn(new ComplaintTimeline());
            when(complaintMapper.toResponse(any())).thenReturn(new ComplaintResponse());
            when(timelineRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
            when(commentRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

            complaintService.createComplaint(request, residentId, societyId);

            verify(slaProperties).computeDeadline(ComplaintPriority.MEDIUM);
        }
    }

    // ── assignComplaint ───────────────────────────────────────────────────────

    @Nested @DisplayName("assignComplaint()")
    class AssignTests {

        @Test @DisplayName("Should assign OPEN complaint successfully")
        void assign_success() {
            AssignComplaintRequest request = AssignComplaintRequest.builder()
                    .assignedToId(vendorId).note("Please fix ASAP").build();

            when(complaintRepository.findByIdAndSocietyId(complaintId, societyId))
                    .thenReturn(Optional.of(openComplaint));
            when(complaintRepository.save(any())).thenReturn(openComplaint);
            when(timelineRepository.save(any())).thenReturn(new ComplaintTimeline());
            when(complaintMapper.toResponse(any())).thenReturn(new ComplaintResponse());
            when(timelineRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
            when(commentRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

            complaintService.assignComplaint(complaintId, request, managerId, societyId);

            assertThat(openComplaint.getStatus()).isEqualTo(ComplaintStatus.ASSIGNED);
            assertThat(openComplaint.getAssignedToId()).isEqualTo(vendorId);
        }

        @Test @DisplayName("Should throw InvalidStatusTransitionException for CLOSED complaint")
        void assign_closedComplaint() {
            openComplaint.setStatus(ComplaintStatus.CLOSED);
            AssignComplaintRequest request = AssignComplaintRequest.builder().assignedToId(vendorId).build();

            when(complaintRepository.findByIdAndSocietyId(complaintId, societyId))
                    .thenReturn(Optional.of(openComplaint));

            assertThatThrownBy(() -> complaintService.assignComplaint(complaintId, request, managerId, societyId))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }

    // ── verifyResolution ──────────────────────────────────────────────────────

    @Nested @DisplayName("verifyResolution()")
    class VerifyTests {

        @BeforeEach
        void setUpPendingVerification() {
            openComplaint.setStatus(ComplaintStatus.PENDING_VERIFICATION);
        }

        @Test @DisplayName("Should CLOSE complaint when resident says YES")
        void verify_resolvedYes_closesComplaint() {
            when(complaintRepository.findByIdAndSocietyId(complaintId, societyId))
                    .thenReturn(Optional.of(openComplaint));
            when(complaintRepository.save(any())).thenReturn(openComplaint);
            when(timelineRepository.save(any())).thenReturn(new ComplaintTimeline());
            when(complaintMapper.toResponse(any())).thenReturn(new ComplaintResponse());
            when(timelineRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
            when(commentRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

            complaintService.verifyResolution(complaintId, true, residentId, societyId);

            assertThat(openComplaint.getStatus()).isEqualTo(ComplaintStatus.CLOSED);
            assertThat(openComplaint.getClosedAt()).isNotNull();
        }

        @Test @DisplayName("Should REOPEN complaint when resident says NO")
        void verify_resolvedNo_reopensComplaint() {
            when(complaintRepository.findByIdAndSocietyId(complaintId, societyId))
                    .thenReturn(Optional.of(openComplaint));
            when(complaintRepository.save(any())).thenReturn(openComplaint);
            when(timelineRepository.save(any())).thenReturn(new ComplaintTimeline());
            when(complaintMapper.toResponse(any())).thenReturn(new ComplaintResponse());
            when(timelineRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
            when(commentRepository.findByComplaintIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

            complaintService.verifyResolution(complaintId, false, residentId, societyId);

            assertThat(openComplaint.getStatus()).isEqualTo(ComplaintStatus.REOPENED);
            assertThat(openComplaint.getReopenCount()).isEqualTo(1);
        }

        @Test @DisplayName("Should throw AccessDeniedException when non-owner tries to verify")
        void verify_wrongResident_throwsAccessDenied() {
            UUID otherResident = UUID.randomUUID();
            when(complaintRepository.findByIdAndSocietyId(complaintId, societyId))
                    .thenReturn(Optional.of(openComplaint));

            assertThatThrownBy(() ->
                    complaintService.verifyResolution(complaintId, true, otherResident, societyId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    // ── SLA breach check ──────────────────────────────────────────────────────

    @Nested @DisplayName("SLA breach detection")
    class SlaTests {

        @Test @DisplayName("Should detect SLA breach when deadline has passed")
        void slaBreached_pastDeadline() {
            openComplaint.setSlaDeadline(LocalDateTime.now().minusHours(1));
            assertThat(openComplaint.isSlaBreached()).isTrue();
        }

        @Test @DisplayName("Should not flag SLA breach for closed complaint")
        void slaBreached_closedComplaint() {
            openComplaint.setSlaDeadline(LocalDateTime.now().minusHours(1));
            openComplaint.setStatus(ComplaintStatus.CLOSED);
            assertThat(openComplaint.isSlaBreached()).isFalse();
        }

        @Test @DisplayName("Should escalate level correctly up to max 3")
        void escalate_incrementsLevel() {
            assertThat(openComplaint.getEscalationLevel()).isZero();
            openComplaint.escalate();
            assertThat(openComplaint.getEscalationLevel()).isEqualTo(1);
            openComplaint.escalate();
            openComplaint.escalate();
            assertThat(openComplaint.getEscalationLevel()).isEqualTo(3);
            openComplaint.escalate(); // should not go above 3
            assertThat(openComplaint.getEscalationLevel()).isEqualTo(3);
        }
    }
}