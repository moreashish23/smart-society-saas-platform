package com.smartsociety.analytics.repository;

import com.smartsociety.analytics.entity.VendorPerformanceStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VendorPerformanceStatsRepository extends JpaRepository<VendorPerformanceStats, UUID> {


    @Query("""
            SELECT v FROM VendorPerformanceStats v
            WHERE v.societyId = :societyId
              AND v.statMonth  = :statMonth
            ORDER BY v.avgRating DESC
            """)
    List<VendorPerformanceStats> findTopVendorsByMonth(
            @Param("societyId") UUID societyId,
            @Param("statMonth")  LocalDate statMonth);
}