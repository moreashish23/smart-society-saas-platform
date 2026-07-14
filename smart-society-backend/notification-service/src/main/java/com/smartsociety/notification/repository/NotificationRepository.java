package com.smartsociety.notification.repository;

import com.smartsociety.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {



    @Query("""
            SELECT n FROM Notification n
            WHERE n.societyId = :societyId
              AND (n.recipientId = :userId OR n.recipientId IS NULL)
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findForUser(
            @Param("userId")    UUID userId,
            @Param("societyId") UUID societyId,
            Pageable pageable);

    @Query("""
            SELECT n FROM Notification n
            WHERE n.societyId = :societyId
              AND (n.recipientId = :userId OR n.recipientId IS NULL)
              AND n.read = false
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findUnreadForUser(
            @Param("userId")    UUID userId,
            @Param("societyId") UUID societyId,
            Pageable pageable);


    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.societyId = :societyId
              AND (n.recipientId = :userId OR n.recipientId IS NULL)
              AND n.read = false
            """)
    long countUnreadForUser(
            @Param("userId")    UUID userId,
            @Param("societyId") UUID societyId);


    @Modifying
    @Query("""
            UPDATE Notification n
            SET    n.read = true
            WHERE  n.societyId = :societyId
              AND  (n.recipientId = :userId OR n.recipientId IS NULL)
              AND  n.read = false
            """)
    int markAllReadForUser(
            @Param("userId")    UUID userId,
            @Param("societyId") UUID societyId);


    @Modifying
    @Query("""
            DELETE FROM Notification n
            WHERE n.createdAt < :cutoff
            """)
    int deleteOlderThan(@Param("cutoff") java.time.LocalDateTime cutoff);
}