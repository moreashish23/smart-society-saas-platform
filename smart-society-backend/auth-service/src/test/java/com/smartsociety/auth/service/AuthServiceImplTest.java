package com.smartsociety.auth.service;

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
import com.smartsociety.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository            userRepository;
    @Mock private RefreshTokenRepository    refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder           passwordEncoder;
    @Mock private JwtTokenProvider          jwtTokenProvider;
    @Mock private UserMapper                userMapper;
    @Mock private AppProperties             appProperties;
    @Mock private JavaMailSender            mailSender;
    @Mock private AuditClient              auditClient;

    @InjectMocks
    private AuthServiceImpl authService;

    // ─── Common fixtures ──────────────────────────────────────────────────────

    private User activeUser;
    private UUID userId;
    private UUID societyId;

    @BeforeEach
    void setUp() {
        userId    = UUID.randomUUID();
        societyId = UUID.randomUUID();

        activeUser = User.builder()
                .id(userId)
                .email("resident@test.com")
                .passwordHash("$2a$12$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.RESIDENT)
                .status(AccountStatus.ACTIVE)
                .societyId(societyId)
                .failedLoginAttempts(0)
                .build();

        AppProperties.Jwt jwtProps = new AppProperties.Jwt();
        jwtProps.setAccessTokenExpirationMs(900_000L);
        jwtProps.setRefreshTokenExpirationMs(604_800_000L);
        lenient().when(appProperties.getJwt()).thenReturn(jwtProps);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should register a resident user successfully")
        void register_success() {
            RegisterRequest request = RegisterRequest.builder()
                    .firstName("Jane").lastName("Smith")
                    .email("jane@test.com").password("Test@1234")
                    .role(UserRole.RESIDENT).societyId(societyId)
                    .build();

            UserResponse expectedResponse = new UserResponse();
            expectedResponse.setId(UUID.randomUUID());

            when(userRepository.existsByEmail("jane@test.com")).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(activeUser);
            when(passwordEncoder.encode("Test@1234")).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(activeUser);
            when(userMapper.toUserResponse(activeUser)).thenReturn(expectedResponse);

            UserResponse result = authService.register(request);

            assertThat(result).isNotNull();
            verify(userRepository).existsByEmail("jane@test.com");
            verify(passwordEncoder).encode("Test@1234");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email is taken")
        void register_emailAlreadyExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("existing@test.com").role(UserRole.RESIDENT)
                    .societyId(societyId).build();

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw SocietyRequiredException for non-SUPER_ADMIN without societyId")
        void register_societyRequiredForResident() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@test.com").role(UserRole.RESIDENT)
                    .societyId(null).build();

            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(SocietyRequiredException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should return AuthResponse on successful login")
        void login_success() {
            LoginRequest request = LoginRequest.builder()
                    .email("resident@test.com").password("Test@1234").build();

            UserResponse userResponse = new UserResponse();
            when(userRepository.findByEmail("resident@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("Test@1234", activeUser.getPasswordHash())).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(activeUser);
            when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("access.token.here");
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());
            when(userMapper.toUserResponse(activeUser)).thenReturn(userResponse);

            AuthResponse result = authService.login(request, "127.0.0.1");

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("access.token.here");
            assertThat(result.getRefreshToken()).isNotNull();
            verify(userRepository).save(activeUser); // last login updated
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException for unknown email")
        void login_unknownEmail() {
            when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().email("ghost@test.com").password("x").build(), "127.0.0.1"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException and increment failed attempts on wrong password")
        void login_wrongPassword_incrementsAttempts() {
            LoginRequest request = LoginRequest.builder()
                    .email("resident@test.com").password("WrongPass!").build();

            when(userRepository.findByEmail("resident@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("WrongPass!", activeUser.getPasswordHash())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(activeUser);

            assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                    .isInstanceOf(InvalidCredentialsException.class);

            assertThat(activeUser.getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should lock account after 5 failed attempts")
        void login_locksAfterMaxFailedAttempts() {
            activeUser = User.builder()
                    .id(userId).email("resident@test.com")
                    .passwordHash("hashed").firstName("John").lastName("Doe")
                    .role(UserRole.RESIDENT).status(AccountStatus.ACTIVE)
                    .societyId(societyId).failedLoginAttempts(4).build();

            LoginRequest request = LoginRequest.builder()
                    .email("resident@test.com").password("wrong").build();

            when(userRepository.findByEmail("resident@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(any(), any())).thenReturn(false);
            when(userRepository.save(any())).thenReturn(activeUser);

            assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                    .isInstanceOf(InvalidCredentialsException.class);

            assertThat(activeUser.getStatus()).isEqualTo(AccountStatus.LOCKED);
            assertThat(activeUser.getLockedUntil()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should throw AccountLockedException when account is locked")
        void login_accountLocked() {
            activeUser.setStatus(AccountStatus.LOCKED);
            activeUser.setLockedUntil(LocalDateTime.now().plusMinutes(20));

            when(userRepository.findByEmail("resident@test.com")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().email("resident@test.com").password("any").build(), "127.0.0.1"))
                    .isInstanceOf(AccountLockedException.class);
        }

        @Test
        @DisplayName("Should throw AccountDisabledException when account is inactive")
        void login_accountInactive() {
            activeUser.setStatus(AccountStatus.INACTIVE);

            when(userRepository.findByEmail("resident@test.com")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().email("resident@test.com").password("any").build(), "127.0.0.1"))
                    .isInstanceOf(AccountDisabledException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should issue new token pair and revoke old refresh token")
        void refreshToken_success() {
            String rawToken = UUID.randomUUID().toString();

            RefreshToken stored = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .user(activeUser)
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();

            UserResponse userResponse = new UserResponse();

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));
            when(refreshTokenRepository.save(any())).thenReturn(stored);
            when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("new.access.token");
            when(userMapper.toUserResponse(activeUser)).thenReturn(userResponse);

            AuthResponse result = authService.refreshToken(
                    new RefreshTokenRequest(rawToken), "127.0.0.1");

            assertThat(result.getAccessToken()).isEqualTo("new.access.token");
            assertThat(stored.getRevoked()).isTrue();
        }

        @Test
        @DisplayName("Should throw InvalidTokenException for non-existent token")
        void refreshToken_notFound() {
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken(
                    new RefreshTokenRequest(UUID.randomUUID().toString()), "127.0.0.1"))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("Should revoke all sessions when expired token reused (possible theft)")
        void refreshToken_expiredTokenRevokesAllSessions() {
            String rawToken = UUID.randomUUID().toString();
            RefreshToken expired = RefreshToken.builder()
                    .id(UUID.randomUUID()).user(activeUser)
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().minusHours(1))   // already expired
                    .build();

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> authService.refreshToken(
                    new RefreshTokenRequest(rawToken), "127.0.0.1"))
                    .isInstanceOf(InvalidTokenException.class);

            verify(refreshTokenRepository).revokeAllTokensByUserId(activeUser.getId());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password and revoke all sessions")
        void changePassword_success() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Old@1234")
                    .newPassword("New@5678")
                    .confirmPassword("New@5678")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("Old@1234", activeUser.getPasswordHash())).thenReturn(true);
            when(passwordEncoder.encode("New@5678")).thenReturn("newHash");
            when(userRepository.save(any())).thenReturn(activeUser);

            authService.changePassword(request, userId.toString());

            assertThat(activeUser.getPasswordHash()).isEqualTo("newHash");
            verify(refreshTokenRepository).revokeAllTokensByUserId(userId);
        }

        @Test
        @DisplayName("Should throw PasswordMismatchException when confirm password differs")
        void changePassword_mismatch() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Old@1234")
                    .newPassword("New@5678")
                    .confirmPassword("Different@9999")
                    .build();

            assertThatThrownBy(() -> authService.changePassword(request, userId.toString()))
                    .isInstanceOf(PasswordMismatchException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidPasswordException when current password is wrong")
        void changePassword_wrongCurrentPassword() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Wrong@1234")
                    .newPassword("New@5678")
                    .confirmPassword("New@5678")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("Wrong@1234", activeUser.getPasswordHash())).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword(request, userId.toString()))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }
}