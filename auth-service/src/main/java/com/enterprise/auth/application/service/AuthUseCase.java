package com.enterprise.auth.application.service;

import com.enterprise.auth.application.dto.AuthResponse;
import com.enterprise.auth.application.dto.LoginRequest;

public interface AuthUseCase {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String expiredAccessToken, String refreshToken);
    void logout(String accessToken);
    void forgotPassword(com.enterprise.auth.application.dto.ForgotPasswordRequest request);
    void resetPassword(com.enterprise.auth.application.dto.PasswordResetRequest request);
}
