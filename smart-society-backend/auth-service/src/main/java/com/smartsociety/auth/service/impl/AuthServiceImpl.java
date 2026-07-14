package com.smartsociety.auth.service.impl;

import com.smartsociety.auth.client.AuditClient;
import com.smartsociety.auth.config.AppProperties;
import com.smartsociety.auth.dto.request.*;
import com.smartsociety.auth.dto.response.AuthResponse;
import com.smartsociety.auth.dto.response.UserResponse;
import com.smartsociety.auth.entity.*;
import com.smartsociety.auth.exception.*;
import com.smartsociety.auth.mapper.UserMapper;
import com.smartsociety.auth.repository.*;
import com.smartsociety.auth.security.JwtTokenProvider;
import com.smartsociety.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository            userRepository;
    private final RefreshTokenRepository    refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder           passwordEncoder;
    private final JwtTokenProvider          jwtTokenProvider;
    private final UserMapper                userMapper;
    private final AppProperties             appProperties;
    private final JavaMailSender            mailSender;
    private final AuditClient              auditClient;


    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }


        if (request.getRole() != UserRole.SUPER_ADMIN && request.getSocietyId() == null) {
            throw new SocietyRequiredException();
        }


        User user = userMapper.toEntity(request);
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);


        sendAuditEvent(saved.getId(), saved.getSocietyId(), "REGISTER",
                "USER", saved.getId(), null, "New user registered: " + saved.getEmail());

        log.info("User registered: id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return userMapper.toUserResponse(saved);
    }


    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);


        if (user.isLocked()) {
            throw new AccountLockedException(
                    "Account locked until " + user.getLockedUntil() + ". Too many failed login attempts.");
        }
        if (!user.isActive()) {
            throw new AccountDisabledException();
        }


        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException();
        }


        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);


        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String rawRefresh   = UUID.randomUUID().toString();
        saveRefreshToken(user, rawRefresh, ipAddress, request.getDeviceInfo());

        sendAuditEvent(user.getId(), user.getSocietyId(), "LOGIN",
                "USER", user.getId(), ipAddress, "Successful login");

        log.info("Login successful: userId={}, role={}", user.getId(), user.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toUserResponse(user))
                .build();
    }


    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        String tokenHash = hashToken(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or already used"));

        if (!stored.isValid()) {

            refreshTokenRepository.revokeAllTokensByUserId(stored.getUser().getId());
            throw new InvalidTokenException("Refresh token has expired or been revoked. Please log in again.");
        }

        User user = stored.getUser();


        stored.revoke();
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRawRefresh  = UUID.randomUUID().toString();
        saveRefreshToken(user, newRawRefresh, ipAddress, stored.getDeviceInfo());

        log.info("Token refreshed for userId={}", user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefresh)
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toUserResponse(user))
                .build();
    }


    @Override
    @Transactional
    public void logout(String rawRefreshToken, String userId) {
        String tokenHash = hashToken(rawRefreshToken);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });

        sendAuditEvent(UUID.fromString(userId), null, "LOGOUT",
                "USER", UUID.fromString(userId), null, "User logged out");

        log.info("User logged out: userId={}", userId);
    }

    @Override
    @Transactional
    public void logoutAll(String userId) {
        UUID uid = UUID.fromString(userId);
        refreshTokenRepository.revokeAllTokensByUserId(uid);

        sendAuditEvent(uid, null, "LOGOUT_ALL",
                "USER", uid, null, "All sessions revoked");

        log.info("All sessions revoked for userId={}", userId);
    }


    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return success to avoid email enumeration attacks
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            // Invalidate any existing unused reset tokens
            passwordResetTokenRepository.invalidateAllActiveTokensForUser(user.getId());

            // Create new token (raw UUID — not hashed, short-lived)
            String rawToken = UUID.randomUUID().toString();
            long expirationMs = appProperties.getPasswordReset().getTokenExpirationMs();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(rawToken)
                    .expiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                    .build();

            passwordResetTokenRepository.save(resetToken);
            sendPasswordResetEmail(user, rawToken);

            log.info("Password reset token generated for userId={}", user.getId());
        });
    }

    // RESET PASSWORD

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Password reset token is invalid"));

        if (!resetToken.isValid()) {
            throw new TokenExpiredException("Password reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedAttempts();
        userRepository.save(user);

        // Mark token as used + revoke all refresh tokens (force re-login everywhere)
        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllTokensByUserId(user.getId());

        sendAuditEvent(user.getId(), user.getSocietyId(), "PASSWORD_RESET",
                "USER", user.getId(), null, "Password reset via email token");

        log.info("Password reset successful for userId={}", user.getId());
    }

    // CHANGE PASSWORD (authenticated)


    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, String userId) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all other sessions for security
        refreshTokenRepository.revokeAllTokensByUserId(user.getId());

        sendAuditEvent(user.getId(), user.getSocietyId(), "PASSWORD_CHANGE",
                "USER", user.getId(), null, "Password changed by user");

        log.info("Password changed for userId={}", userId);
    }


    // GET PROFILE


    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }


    // PRIVATE HELPERS


    private void handleFailedLogin(User user) {
        user.incrementFailedAttempts();

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.lockAccount(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("Account locked due to {} failed attempts: userId={}", MAX_FAILED_ATTEMPTS, user.getId());
        }

        userRepository.save(user);
    }

    private void saveRefreshToken(User user, String rawToken, String ipAddress, String deviceInfo) {
        long expirationMs = appProperties.getJwt().getRefreshTokenExpirationMs();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .build();

        refreshTokenRepository.save(token);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Async
    void sendPasswordResetEmail(User user, String rawToken) {
        try {
            String resetUrl = appProperties.getPasswordReset().getFrontendUrl()
                    + "/reset-password?token=" + rawToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Smart Society — Password Reset Request");
            message.setText("""
                    Dear %s,
                    
                    You requested a password reset for your Smart Society account.
                    
                    Click the link below to reset your password (expires in 15 minutes):
                    %s
                    
                    If you did not request this, please ignore this email.
                    Your account remains secure.
                    
                    — Smart Society Team
                    """.formatted(user.getFirstName(), resetUrl));

            mailSender.send(message);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), ex.getMessage());
        }
    }

    @Async
    void sendAuditEvent(UUID userId, UUID societyId, String action,
                        String entityType, UUID entityId, String ipAddress, String description) {
        try {
            auditClient.logEvent(AuditClient.AuditEventRequest.builder()
                    .userId(userId)
                    .societyId(societyId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(ipAddress)
                    .description(description)
                    .build());
        } catch (Exception ex) {
            log.warn("Audit event could not be sent: {}", ex.getMessage());
        }
    }
}