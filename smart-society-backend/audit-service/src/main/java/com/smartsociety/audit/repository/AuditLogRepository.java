package com.smartsociety.audit.repository;

import com.smartsociety.audit.entity.AuditAction;
import com.smartsociety.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;


@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>,
        JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findBySocietyIdOrderByCreatedAtDesc(UUID societyId, Pageable pageable);

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findBySocietyIdAndActionOrderByCreatedAtDesc(
            UUID societyId, AuditAction action, Pageable pageable);

    Page<AuditLog> findBySocietyIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            UUID societyId, String entityType, UUID entityId, Pageable pageable);


    @Modifying
    @Query(value = "DELETE FROM audit_logs WHERE created_at < :cutoff", nativeQuery = true)
    int deleteLogsOlderThan(@Param("cutoff") LocalDateTime cutoff);

    long countBySocietyId(UUID societyId);

    long countBySocietyIdAndAction(UUID societyId, AuditAction action);
}