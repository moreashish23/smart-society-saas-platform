package com.smartsociety.society.repository;

import com.smartsociety.society.entity.Society;
import com.smartsociety.society.entity.SocietyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SocietyRepository extends JpaRepository<Society, UUID> {


    boolean existsByCode(String code);


    boolean existsByCodeAndIdNot(String code, UUID id);


    @Query("""
            SELECT s FROM Society s
            WHERE (:status IS NULL OR s.status = :status)
              AND (:name   IS NULL
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
            """)
    Page<Society> searchSocieties(
            @Param("status") SocietyStatus status,
            @Param("name")   String name,
            Pageable pageable);


    @Query("""
            SELECT COUNT(m) FROM SocietyMember m
            WHERE m.society.id = :societyId
              AND m.status = com.smartsociety.society.entity.MemberStatus.ACTIVE
            """)
    long countActiveMembers(@Param("societyId") UUID societyId);
}