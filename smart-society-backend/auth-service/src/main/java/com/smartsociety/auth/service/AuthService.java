package com.smartsociety.auth.service;

import com.smartsociety.auth.dto.request.*;
import com.smartsociety.auth.dto.response.AuthResponse;
import com.smartsociety.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String ipAddress);
    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress);
    void         logout(String rawRefreshToken, String userId);
    void         logoutAll(String userId);
    void         forgotPassword(ForgotPasswordRequest request);
    void         resetPassword(ResetPasswordRequest request);
    void         changePassword(ChangePasswordRequest request, String userId);
    UserResponse getProfile(String userId);
}