package com.smartsociety.auth.scheduler;

import com.smartsociety.auth.repository.PasswordResetTokenRepository;
import com.smartsociety.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository       refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        try {
            refreshTokenRepository.deleteExpiredAndRevokedTokens(now);
            log.info("Cleaned up expired/revoked refresh tokens at {}", now);
        } catch (Exception ex) {
            log.error("Error cleaning refresh tokens: {}", ex.getMessage());
        }

        try {
            passwordResetTokenRepository.deleteExpiredAndUsedTokens(now);
            log.info("Cleaned up expired/used password reset tokens at {}", now);
        } catch (Exception ex) {
            log.error("Error cleaning password reset tokens: {}", ex.getMessage());
        }
    }
}