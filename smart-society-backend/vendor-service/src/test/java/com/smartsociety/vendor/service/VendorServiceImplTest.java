package com.smartsociety.vendor.service;

import com.smartsociety.vendor.client.AuditClient;
import com.smartsociety.vendor.dto.request.*;
import com.smartsociety.vendor.dto.response.*;
import com.smartsociety.vendor.entity.*;
import com.smartsociety.vendor.exception.*;
import com.smartsociety.vendor.mapper.VendorMapper;
import com.smartsociety.vendor.repository.*;
import com.smartsociety.vendor.service.impl.VendorServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorServiceImpl Unit Tests")
class VendorServiceImplTest {

    @Mock private VendorRepository       vendorRepository;
    @Mock private VendorJobRepository    jobRepository;
    @Mock private VendorRatingRepository ratingRepository;
    @Mock private VendorMapper           vendorMapper;
    @Mock private AuditClient            auditClient;

    @InjectMocks private VendorServiceImpl vendorService;

    private UUID vendorId, societyId, managerId, residentId, userId;
    private Vendor vendor;

    @BeforeEach
    void setUp() {
        vendorId   = UUID.randomUUID();
        societyId  = UUID.randomUUID();
        managerId  = UUID.randomUUID();
        residentId = UUID.randomUUID();
        userId     = UUID.randomUUID();

        vendor = Vendor.builder()
                .id(vendorId).societyId(societyId).userId(userId)
                .businessName("Plumbing Pro").contactPerson("Rajesh Kumar")
                .contactEmail("rajesh@plumbingpro.com").contactPhone("9876543210")
                .serviceCategory(ServiceCategory.PLUMBING)
                .status(VendorStatus.ACTIVE).rating(BigDecimal.valueOf(4.50))
                .totalJobs(10).completedJobs(9)
                .build();
    }

    @Nested @DisplayName("registerVendor()")
    class RegisterTests {

        @Test @DisplayName("Should register vendor successfully")
        void register_success() {
            CreateVendorRequest request = CreateVendorRequest.builder()
                    .userId(userId).businessName("Plumbing Pro")
                    .contactPerson("Rajesh").contactEmail("r@pp.com")
                    .contactPhone("9876543210").serviceCategory(ServiceCategory.PLUMBING).build();

            VendorResponse expected = new VendorResponse();
            expected.setId(vendorId);

            when(vendorRepository.existsByUserIdAndSocietyId(userId, societyId)).thenReturn(false);
            when(vendorMapper.toEntity(request)).thenReturn(vendor);
            when(vendorRepository.save(any())).thenReturn(vendor);
            when(vendorMapper.toResponse(vendor)).thenReturn(expected);

            VendorResponse result = vendorService.registerVendor(request, societyId, managerId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(vendorId);
            verify(vendorRepository).save(any(Vendor.class));
        }

        @Test @DisplayName("Should throw VendorAlreadyExistsException for duplicate registration")
        void register_duplicate() {
            CreateVendorRequest request = CreateVendorRequest.builder()
                    .userId(userId).serviceCategory(ServiceCategory.PLUMBING).build();

            when(vendorRepository.existsByUserIdAndSocietyId(userId, societyId)).thenReturn(true);

            assertThatThrownBy(() -> vendorService.registerVendor(request, societyId, managerId))
                    .isInstanceOf(VendorAlreadyExistsException.class);
            verify(vendorRepository, never()).save(any());
        }
    }

    @Nested @DisplayName("approveVendor()")
    class ApproveTests {

        @Test @DisplayName("Should approve a PENDING_APPROVAL vendor")
        void approve_success() {
            vendor.setStatus(VendorStatus.PENDING_APPROVAL);
            VendorResponse expected = new VendorResponse();
            expected.setStatus(VendorStatus.ACTIVE);

            when(vendorRepository.findByIdAndSocietyId(vendorId, societyId)).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any())).thenReturn(vendor);
            when(vendorMapper.toResponse(vendor)).thenReturn(expected);

            VendorResponse result = vendorService.approveVendor(vendorId, managerId, societyId);

            assertThat(vendor.getStatus()).isEqualTo(VendorStatus.ACTIVE);
            assertThat(vendor.getApprovedBy()).isEqualTo(managerId);
            assertThat(vendor.getApprovedAt()).isNotNull();
        }
    }

    @Nested @DisplayName("rateVendor()")
    class RatingTests {

        // FILE: vendor-service/src/test/java/com/smartsociety/vendor/service/VendorServiceImplTest.java

        @Test
        @DisplayName("Should submit rating and recalculate aggregate")
        void rate_success() {
            UUID complaintId = UUID.randomUUID();
            RateVendorRequest request = RateVendorRequest.builder()
                    .complaintId(complaintId).rating(5).review("Excellent work!").build();

            VendorRatingResponse expected = new VendorRatingResponse();

            when(vendorRepository.findByIdAndSocietyId(vendorId, societyId)).thenReturn(Optional.of(vendor));
            when(ratingRepository.existsByVendorIdAndComplaintId(vendorId, complaintId)).thenReturn(false);

            // FIX: use any(VendorRating.class) — the service builds its own instance internally
            when(ratingRepository.save(any(VendorRating.class))).thenAnswer(invocation -> invocation.getArgument(0));

            when(vendorRepository.calculateAverageRating(vendorId))
                    .thenReturn(Optional.of(BigDecimal.valueOf(4.75)));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            // FIX: use any(VendorRating.class) — can't match the exact internally-created instance
            when(vendorMapper.toRatingResponse(any(VendorRating.class))).thenReturn(expected);

            VendorRatingResponse result = vendorService.rateVendor(vendorId, request, residentId, societyId);

            assertThat(result).isNotNull();
            verify(vendorRepository).save(argThat(v -> v.getRating().compareTo(BigDecimal.valueOf(4.75)) == 0));
        }

        @Test @DisplayName("Should throw DuplicateRatingException for already-rated complaint")
        void rate_duplicate() {
            UUID complaintId = UUID.randomUUID();
            RateVendorRequest request = RateVendorRequest.builder()
                    .complaintId(complaintId).rating(4).build();

            when(vendorRepository.findByIdAndSocietyId(vendorId, societyId)).thenReturn(Optional.of(vendor));
            when(ratingRepository.existsByVendorIdAndComplaintId(vendorId, complaintId)).thenReturn(true);

            assertThatThrownBy(() -> vendorService.rateVendor(vendorId, request, residentId, societyId))
                    .isInstanceOf(DuplicateRatingException.class);
        }
    }

    @Nested @DisplayName("updateJobStatus()")
    class JobStatusTests {

        @Test @DisplayName("Should mark job COMPLETED and increment vendor completedJobs")
        void updateJobStatus_completed() {
            UUID jobId = UUID.randomUUID();
            VendorJob job = VendorJob.builder()
                    .id(jobId).vendor(vendor)
                    .complaintId(UUID.randomUUID())
                    .status(JobStatus.IN_PROGRESS).build();

            VendorJobResponse expected = new VendorJobResponse();

            when(vendorRepository.findByIdAndSocietyId(vendorId, societyId)).thenReturn(Optional.of(vendor));
            when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
            when(jobRepository.save(any())).thenReturn(job);
            when(vendorRepository.save(any())).thenReturn(vendor);
            when(vendorMapper.toJobResponse(job)).thenReturn(expected);

            vendorService.updateJobStatus(vendorId, jobId, JobStatus.COMPLETED,
                    managerId, societyId);

            assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
            assertThat(job.getCompletedAt()).isNotNull();
            assertThat(vendor.getCompletedJobs()).isEqualTo(10);
        }
    }
}