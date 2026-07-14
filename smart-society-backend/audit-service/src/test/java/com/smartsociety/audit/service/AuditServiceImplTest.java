package com.smartsociety.audit.service;

import com.smartsociety.audit.dto.request.LogEventRequest;
import com.smartsociety.audit.dto.response.AuditLogResponse;
import com.smartsociety.audit.entity.AuditAction;
import com.smartsociety.audit.entity.AuditLog;
import com.smartsociety.audit.exception.AuditLogNotFoundException;
import com.smartsociety.audit.mapper.AuditLogMapper;
import com.smartsociety.audit.repository.AuditLogRepository;
import com.smartsociety.audit.service.impl.AuditServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditServiceImpl Unit Tests")
class AuditServiceImplTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AuditLogMapper     auditLogMapper;

    @InjectMocks private AuditServiceImpl auditService;

    private UUID societyId;
    private UUID userId;
    private UUID logId;
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        societyId = UUID.randomUUID();
        userId    = UUID.randomUUID();
        logId     = UUID.randomUUID();

        auditLog = AuditLog.builder()
                .id(logId).societyId(societyId).userId(userId)
                .action(AuditAction.LOGIN).entityType("USER").entityId(userId)
                .description("User logged in successfully").ipAddress("192.168.1.1")
                .build();
    }

    @Nested @DisplayName("logEvent()")
    class LogEventTests {

        @Test @DisplayName("Should create immutable audit log with correct fields")
        void logEvent_createsRecord() {
            LogEventRequest request = LogEventRequest.builder()
                    .userId(userId).societyId(societyId)
                    .action(AuditAction.LOGIN)
                    .entityType("USER").entityId(userId)
                    .description("User logged in").ipAddress("10.0.0.1")
                    .build();

            AuditLogResponse expected = AuditLogResponse.builder()
                    .id(logId).userId(userId).societyId(societyId)
                    .action(AuditAction.LOGIN).build();

            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
            when(auditLogMapper.toResponse(auditLog)).thenReturn(expected);

            AuditLogResponse result = auditService.logEvent(request);

            assertThat(result).isNotNull();
            assertThat(result.getAction()).isEqualTo(AuditAction.LOGIN);
            assertThat(result.getUserId()).isEqualTo(userId);

            verify(auditLogRepository).save(argThat(log ->
                    AuditAction.LOGIN.equals(log.getAction()) &&
                            userId.equals(log.getUserId()) &&
                            societyId.equals(log.getSocietyId())));
        }

        @Test @DisplayName("Should accept log event with null societyId for SUPER_ADMIN actions")
        void logEvent_superAdminNullSociety() {
            LogEventRequest request = LogEventRequest.builder()
                    .userId(userId).societyId(null)
                    .action(AuditAction.SOCIETY_CREATE)
                    .description("New society created by super admin")
                    .build();

            AuditLog savedLog = AuditLog.builder()
                    .id(logId).userId(userId).societyId(null)
                    .action(AuditAction.SOCIETY_CREATE).build();

            AuditLogResponse expected = new AuditLogResponse();
            expected.setId(logId);

            when(auditLogRepository.save(any())).thenReturn(savedLog);
            when(auditLogMapper.toResponse(savedLog)).thenReturn(expected);

            AuditLogResponse result = auditService.logEvent(request);
            assertThat(result.getId()).isEqualTo(logId);
        }
    }

    @Nested @DisplayName("getLog()")
    class GetLogTests {

        @Test @DisplayName("Should return audit log for valid ID")
        void getLog_success() {
            AuditLogResponse expected = AuditLogResponse.builder()
                    .id(logId).action(AuditAction.LOGIN).build();

            when(auditLogRepository.findById(logId)).thenReturn(Optional.of(auditLog));
            when(auditLogMapper.toResponse(auditLog)).thenReturn(expected);

            AuditLogResponse result = auditService.getLog(logId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(logId);
        }

        @Test @DisplayName("Should throw AuditLogNotFoundException for unknown ID")
        void getLog_notFound() {
            when(auditLogRepository.findById(logId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> auditService.getLog(logId))
                    .isInstanceOf(AuditLogNotFoundException.class);
        }
    }

    @Nested @DisplayName("getLogs() search")
    class SearchTests {

        @Test @DisplayName("Should return paged results with filters applied")
        void getLogs_withFilters() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            AuditLogResponse dto = new AuditLogResponse();

            when(auditLogRepository.search(
                    eq(societyId), isNull(), eq(AuditAction.LOGIN),
                    isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(page);
            when(auditLogMapper.toResponse(auditLog)).thenReturn(dto);

            var result = auditService.getLogs(
                    societyId, null, AuditAction.LOGIN,
                    null, null, null, null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested @DisplayName("purgeOldLogs()")
    class PurgeTests {

        @Test @DisplayName("Should call deleteLogsOlderThan with correct cutoff date")
        void purgeOldLogs_callsRepository() {
            when(auditLogRepository.deleteLogsOlderThan(any(LocalDateTime.class))).thenReturn(42);

            int deleted = auditService.purgeOldLogs(365);

            assertThat(deleted).isEqualTo(42);
            verify(auditLogRepository).deleteLogsOlderThan(
                    argThat(cutoff -> cutoff.isBefore(LocalDateTime.now().minusDays(364))));
        }

        @Test @DisplayName("Should return 0 when no logs are old enough to purge")
        void purgeOldLogs_nothingToDelete() {
            when(auditLogRepository.deleteLogsOlderThan(any())).thenReturn(0);

            int deleted = auditService.purgeOldLogs(365);

            assertThat(deleted).isZero();
        }
    }
}