package com.smartsociety.auth.repository;

import com.smartsociety.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {


    Optional<RefreshToken> findByTokenHash(String tokenHash);


    @Modifying
    @Query("""
           UPDATE RefreshToken t
           SET    t.revoked = true
           WHERE  t.user.id = :userId
             AND  t.revoked  = false
           """)
    void revokeAllTokensByUserId(@Param("userId") UUID userId);


    @Modifying
    @Query("""
           DELETE FROM RefreshToken t
           WHERE t.expiresAt < :now
              OR t.revoked   = true
           """)
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);
}