package com.enterprise.auth.domain.repository;

public interface TokenRepository {
    void saveRefreshToken(String userId, String refreshToken, long durationMs);
    boolean existsByUserIdAndRefreshToken(String userId, String refreshToken);
    void revokeRefreshToken(String userId);
    
    void savePasswordResetToken(String token, String email, long durationMs);
    String getEmailByPasswordResetToken(String token);
    void deletePasswordResetToken(String token);
}
