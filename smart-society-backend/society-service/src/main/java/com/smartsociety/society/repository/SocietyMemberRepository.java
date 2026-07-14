package com.smartsociety.society.repository;

import com.smartsociety.society.entity.MemberStatus;
import com.smartsociety.society.entity.SocietyMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SocietyMemberRepository extends JpaRepository<SocietyMember, UUID> {


    boolean existsBySocietyIdAndUserId(UUID societyId, UUID userId);


    @Query("""
            SELECT m FROM SocietyMember m
            WHERE m.society.id = :societyId
              AND (:role   IS NULL OR m.role   = :role)
              AND (:status IS NULL OR m.status = :status)
            """)
    Page<SocietyMember> findMembers(
            @Param("societyId") UUID societyId,
            @Param("role")      String role,
            @Param("status")    MemberStatus status,
            Pageable pageable);
}