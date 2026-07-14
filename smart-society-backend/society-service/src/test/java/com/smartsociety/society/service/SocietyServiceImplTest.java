package com.smartsociety.society.service;

import com.smartsociety.society.client.AuditClient;
import com.smartsociety.society.dto.request.CreateSocietyRequest;
import com.smartsociety.society.dto.request.UpdateSocietyRequest;
import com.smartsociety.society.dto.response.SocietyResponse;
import com.smartsociety.society.entity.*;
import com.smartsociety.society.exception.*;
import com.smartsociety.society.mapper.SocietyMapper;
import com.smartsociety.society.repository.SocietyRepository;
import com.smartsociety.society.service.impl.SocietyServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocietyServiceImpl Unit Tests")
class SocietyServiceImplTest {

    @Mock private SocietyRepository societyRepository;
    @Mock private SocietyMapper     societyMapper;
    @Mock private AuditClient       auditClient;

    @InjectMocks private SocietyServiceImpl societyService;

    private Society     society;
    private UUID        societyId;
    private UUID        adminId;

    @BeforeEach
    void setUp() {
        societyId = UUID.randomUUID();
        adminId   = UUID.randomUUID();

        society = Society.builder()
                .id(societyId)
                .name("Green Valley Society")
                .code("GV001")
                .city("Pune").state("Maharashtra").pincode("411001")
                .addressLine1("12 Green Valley Road")
                .contactEmail("admin@greenvalley.com")
                .contactPhone("9876543201")
                .status(SocietyStatus.ACTIVE)
                .subscriptionPlan(SubscriptionPlan.BASIC)
                .totalUnits(120).totalFloors(10)
                .createdBy(adminId)
                .build();
    }

    // ── createSociety ─────────────────────────────────────────────────────────

    @Nested @DisplayName("createSociety()")
    class CreateTests {

        @Test @DisplayName("Should create society and return response")
        void createSociety_success() {
            CreateSocietyRequest request = CreateSocietyRequest.builder()
                    .name("Green Valley Society").code("GV001")
                    .addressLine1("12 Green Valley Road")
                    .city("Pune").state("Maharashtra").pincode("411001")
                    .contactEmail("admin@greenvalley.com").contactPhone("9876543201")
                    .build();

            SocietyResponse expectedResponse = new SocietyResponse();
            expectedResponse.setId(societyId);
            expectedResponse.setName("Green Valley Society");

            when(societyRepository.existsByCode("GV001")).thenReturn(false);
            when(societyMapper.toEntity(request)).thenReturn(society);
            when(societyRepository.save(any(Society.class))).thenReturn(society);
            when(societyMapper.toResponse(society)).thenReturn(expectedResponse);
            when(societyRepository.countActiveMembers(societyId)).thenReturn(0L);

            SocietyResponse result = societyService.createSociety(request, adminId);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Green Valley Society");
            verify(societyRepository).save(any(Society.class));
        }

        @Test @DisplayName("Should throw SocietyCodeAlreadyExistsException for duplicate code")
        void createSociety_duplicateCode() {
            CreateSocietyRequest request = CreateSocietyRequest.builder().code("GV001").build();
            when(societyRepository.existsByCode("GV001")).thenReturn(true);

            assertThatThrownBy(() -> societyService.createSociety(request, adminId))
                    .isInstanceOf(SocietyCodeAlreadyExistsException.class)
                    .hasMessageContaining("GV001");

            verify(societyRepository, never()).save(any());
        }
    }

    // ── getSociety ────────────────────────────────────────────────────────────

    @Nested @DisplayName("getSociety()")
    class GetTests {

        @Test @DisplayName("Should return society response for valid ID")
        void getSociety_success() {
            SocietyResponse expected = new SocietyResponse();
            expected.setId(societyId);

            when(societyRepository.findById(societyId)).thenReturn(Optional.of(society));
            when(societyMapper.toResponse(society)).thenReturn(expected);
            when(societyRepository.countActiveMembers(societyId)).thenReturn(5L);

            SocietyResponse result = societyService.getSociety(societyId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(societyId);
        }

        @Test @DisplayName("Should throw SocietyNotFoundException for unknown ID")
        void getSociety_notFound() {
            when(societyRepository.findById(societyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> societyService.getSociety(societyId))
                    .isInstanceOf(SocietyNotFoundException.class);
        }
    }

    // ── activate / deactivate ─────────────────────────────────────────────────

    @Nested @DisplayName("activate/deactivate")
    class StatusTests {

        @Test @DisplayName("Should activate an inactive society")
        void activateSociety_success() {
            society.setStatus(SocietyStatus.INACTIVE);
            SocietyResponse expected = new SocietyResponse();
            expected.setStatus(SocietyStatus.ACTIVE);

            when(societyRepository.findById(societyId)).thenReturn(Optional.of(society));
            when(societyRepository.save(any())).thenReturn(society);
            when(societyMapper.toResponse(society)).thenReturn(expected);
            when(societyRepository.countActiveMembers(societyId)).thenReturn(0L);

            SocietyResponse result = societyService.activateSociety(societyId, adminId);

            assertThat(society.getStatus()).isEqualTo(SocietyStatus.ACTIVE);
            verify(societyRepository).save(society);
        }

        @Test @DisplayName("Should deactivate an active society")
        void deactivateSociety_success() {
            SocietyResponse expected = new SocietyResponse();
            expected.setStatus(SocietyStatus.INACTIVE);

            when(societyRepository.findById(societyId)).thenReturn(Optional.of(society));
            when(societyRepository.save(any())).thenReturn(society);
            when(societyMapper.toResponse(society)).thenReturn(expected);
            when(societyRepository.countActiveMembers(societyId)).thenReturn(0L);

            societyService.deactivateSociety(societyId, adminId);

            assertThat(society.getStatus()).isEqualTo(SocietyStatus.INACTIVE);
        }
    }

    // ── getAllSocieties ────────────────────────────────────────────────────────

    @Nested @DisplayName("getAllSocieties()")
    class ListTests {

        @Test @DisplayName("Should return paged societies")
        void getAllSocieties_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Society> page = new PageImpl<>(List.of(society), pageable, 1);
            SocietyResponse dto = new SocietyResponse();

            when(societyRepository.searchSocieties(null, null, pageable)).thenReturn(page);
            when(societyMapper.toResponse(society)).thenReturn(dto);
            when(societyRepository.countActiveMembers(any())).thenReturn(3L);

            var result = societyService.getAllSocieties(null, null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
        }
    }
}