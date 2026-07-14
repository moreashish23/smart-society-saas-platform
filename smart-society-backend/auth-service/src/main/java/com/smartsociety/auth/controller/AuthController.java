package com.smartsociety.auth.controller;

import com.smartsociety.auth.dto.request.*;
import com.smartsociety.auth.dto.response.ApiResponse;
import com.smartsociety.auth.dto.response.AuthResponse;
import com.smartsociety.auth.dto.response.UserResponse;
import com.smartsociety.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Register, login, logout, token refresh, and password management")
public class AuthController {

    private final AuthService authService;

    // ─── REGISTER ─────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Creates a new user account. societyId is required for all roles except SUPER_ADMIN.")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User registered successfully"));
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens",
            description = "Returns accessToken (15 min) and refreshToken (7 days). Include accessToken as Bearer in subsequent requests.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse auth = authService.login(request, extractClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(auth, "Login successful"));
    }

    // ─── REFRESH TOKEN ────────────────────────────────────────────────────────

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
            description = "Exchanges a valid refreshToken for a new accessToken + rotated refreshToken. Old refreshToken is revoked.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse auth = authService.refreshToken(request, extractClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(auth, "Token refreshed successfully"));
    }

    // ─── LOGOUT ───────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(summary = "Logout current session",
            description = "Revokes the provided refresh token. Access token remains valid until natural expiry.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        authService.logout(request.getRefreshToken(), userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices",
            description = "Revokes all active refresh tokens for the current user (all sessions terminated).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAll(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        authService.logoutAll(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out from all devices"));
    }

    // ─── FORGOT PASSWORD ──────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email",
            description = "Sends a password reset link to the registered email. Always returns 200 to prevent email enumeration.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null,
                "If that email is registered, a password reset link has been sent."));
    }

    // ─── RESET PASSWORD ───────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using email token",
            description = "Validates the token from the email link and updates the password. Token is single-use and expires in 15 minutes.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully. Please log in with your new password."));
    }

    // ─── CHANGE PASSWORD ──────────────────────────────────────────────────────

    @PostMapping("/change-password")
    @Operation(summary = "Change password (authenticated)",
            description = "Changes the password for the currently authenticated user. Requires current password. All other sessions are revoked.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        authService.changePassword(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully. Please log in again."));
    }

    // ─── GET PROFILE ──────────────────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
            description = "Returns the authenticated user's profile details.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        UserResponse user = authService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────────────


    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}