package com.smartsociety.analytics.repository;

import com.smartsociety.analytics.entity.DailyComplaintStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyComplaintStatsRepository extends JpaRepository<DailyComplaintStats, UUID> {


    List<DailyComplaintStats> findBySocietyIdAndStatDateBetweenOrderByStatDateAsc(
            UUID societyId, LocalDate from, LocalDate to);


    Optional<DailyComplaintStats> findBySocietyIdAndStatDate(UUID societyId, LocalDate statDate);


    @Query(
            value = """
                SELECT DISTINCT society_id
                FROM daily_complaint_stats
                WHERE stat_date >= :from
                  AND stat_date <= :to
                """,
            nativeQuery = true
    )
    List<UUID> findDistinctSocietyIds(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);
}