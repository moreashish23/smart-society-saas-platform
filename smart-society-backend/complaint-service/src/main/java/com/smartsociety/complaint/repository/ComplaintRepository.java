package com.smartsociety.complaint.repository;

import com.smartsociety.complaint.entity.Complaint;
import com.smartsociety.complaint.entity.ComplaintCategory;
import com.smartsociety.complaint.entity.ComplaintPriority;
import com.smartsociety.complaint.entity.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    // ── Core lookups ──────────────────────────────────────────────────────────

    Optional<Complaint> findByIdAndSocietyId(UUID id, UUID societyId);

    // ── Paged listings ────────────────────────────────────────────────────────

    Page<Complaint> findBySocietyId(UUID societyId, Pageable pageable);

    Page<Complaint> findBySocietyIdAndStatus(UUID societyId, ComplaintStatus status, Pageable pageable);

    Page<Complaint> findBySocietyIdAndCategory(UUID societyId, ComplaintCategory category, Pageable pageable);

    Page<Complaint> findBySocietyIdAndPriority(UUID societyId, ComplaintPriority priority, Pageable pageable);

    /**
     * Used by ComplaintServiceImpl.getMyComplaints()
     */
    Page<Complaint> findBySocietyIdAndResidentId(UUID societyId, UUID residentId, Pageable pageable);

    /**
     * Used by ComplaintServiceImpl.getAssignedComplaints()
     */
    Page<Complaint> findByAssignedToId(UUID assignedToId, Pageable pageable);


    @Query("""
            SELECT c FROM Complaint c
            WHERE c.societyId = :societyId
              AND (:status   IS NULL OR c.status   = :status)
              AND (:category IS NULL OR c.category = :category)
              AND (:priority IS NULL OR c.priority = :priority)
              AND (:keyword  IS NULL
                   OR LOWER(c.title)       LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
            ORDER BY c.createdAt DESC
            """)
    Page<Complaint> searchComplaints(
            @Param("societyId") UUID societyId,
            @Param("status")    ComplaintStatus status,
            @Param("category")  ComplaintCategory category,
            @Param("priority")  ComplaintPriority priority,
            @Param("keyword")   String keyword,
            Pageable pageable);

    // ── Escalation engine ─────────────────────────────────────────────────────


    @Query("""
            SELECT c FROM Complaint c
            WHERE c.status NOT IN :terminalStatuses
              AND c.slaDeadline < :now
              AND c.escalationLevel < 3
            ORDER BY c.slaDeadline ASC
            """)
    List<Complaint> findBreachedComplaintsForEscalation(
            @Param("now")             LocalDateTime now,
            @Param("terminalStatuses") List<ComplaintStatus> terminalStatuses);

    // ── Analytics count helpers ───────────────────────────────────────────────


    long countBySocietyIdAndStatus(UUID societyId, ComplaintStatus status);


    long countBySocietyIdAndPriority(UUID societyId, ComplaintPriority priority);


    @Query("""
            SELECT COUNT(c) FROM Complaint c
            WHERE c.societyId = :societyId
              AND c.slaDeadline < CURRENT_TIMESTAMP
              AND c.status NOT IN :terminalStatuses
            """)
    long countSlaBreaches(
            @Param("societyId")        UUID societyId,
            @Param("terminalStatuses") List<ComplaintStatus> terminalStatuses);


    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.societyId = :societyId AND c.reopenCount > 0")
    long countReopened(@Param("societyId") UUID societyId);


    @Query(
            value = """
                SELECT AVG(EXTRACT(EPOCH FROM (closed_at - created_at)) / 3600.0)
                FROM   complaints
                WHERE  society_id = :societyId
                  AND  status     = 'CLOSED'
                  AND  closed_at  IS NOT NULL
                """,
            nativeQuery = true
    )
    Double avgResolutionHours(@Param("societyId") UUID societyId);

    // ── Trend queries used by ComplaintAnalyticsController ────────────────────

    @Query("""
            SELECT COUNT(c) FROM Complaint c
            WHERE c.societyId = :societyId
              AND c.createdAt >= :from
              AND c.createdAt  < :to
            """)
    long countCreatedBetween(
            @Param("societyId") UUID societyId,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to);

    @Query("""
            SELECT COUNT(c) FROM Complaint c
            WHERE c.societyId = :societyId
              AND c.closedAt >= :from
              AND c.closedAt  < :to
            """)
    long countClosedBetween(
            @Param("societyId") UUID societyId,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to);

    @Query("""
            SELECT COUNT(c) FROM Complaint c
            WHERE c.societyId    = :societyId
              AND c.escalatedAt >= :from
              AND c.escalatedAt  < :to
              AND c.escalationLevel > 0
            """)
    long countEscalatedBetween(
            @Param("societyId") UUID societyId,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to);
}