package com.smartsociety.society.repository;

import com.smartsociety.society.entity.Notice;
import com.smartsociety.society.entity.NoticeStatus;
import com.smartsociety.society.entity.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, UUID> {

    Optional<Notice> findByIdAndSocietyId(UUID id, UUID societyId);

    Page<Notice> findBySocietyId(UUID societyId, Pageable pageable);

    Page<Notice> findBySocietyIdAndStatus(UUID societyId, NoticeStatus status, Pageable pageable);

    Page<Notice> findBySocietyIdAndNoticeType(UUID societyId, NoticeType noticeType, Pageable pageable);


    @Query("""
            SELECT n FROM Notice n
            WHERE n.society.id = :societyId
              AND n.status     = com.smartsociety.society.entity.NoticeStatus.PUBLISHED
              AND (n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP)
            ORDER BY n.priority DESC, n.publishedAt DESC
            """)
    List<Notice> findActiveNotices(@Param("societyId") UUID societyId);
}