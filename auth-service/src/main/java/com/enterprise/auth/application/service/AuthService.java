package com.enterprise.auth.application.service;

import com.enterprise.auth.application.dto.AuthResponse;
import com.enterprise.auth.application.dto.LoginRequest;
import com.enterprise.auth.domain.model.AuthToken;
import com.enterprise.auth.domain.model.User;
import com.enterprise.auth.domain.repository.TokenRepository;
import com.enterprise.auth.domain.repository.UserRepository;
import com.enterprise.auth.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.Instant.now())) {
            throw new RuntimeException("Account is locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(java.time.Instant.now().plus(15, java.time.temporal.ChronoUnit.MINUTES));
            }
            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        if (user.isMfaEnabled()) {
            String tempToken = jwtTokenProvider.generateTempToken(user);
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .tempToken(tempToken)
                    .build();
        }

        AuthToken token = jwtTokenProvider.generateToken(user);
        
        tokenRepository.saveRefreshToken(user.getId().toString(), token.getRefreshToken(), 604800000L); // 7 days in ms

        return AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .expiresIn(token.getExpiresIn())
                .tokenType("Bearer")
                .mfaRequired(false)
                .build();
    }

    @Override
    public AuthResponse refresh(String expiredAccessToken, String refreshToken) {
        String userIdStr = jwtTokenProvider.getUserIdFromExpiredToken(expiredAccessToken);
        if (!tokenRepository.existsByUserIdAndRefreshToken(userIdStr, refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        User user = userRepository.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthToken token = jwtTokenProvider.generateToken(user);
        tokenRepository.saveRefreshToken(user.getId().toString(), token.getRefreshToken(), 604800000L);

        return AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .expiresIn(token.getExpiresIn())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(String accessToken) {
        String userIdStr = jwtTokenProvider.getUserIdFromExpiredToken(accessToken);
        tokenRepository.revokeRefreshToken(userIdStr);
    }

    @Override
    public void forgotPassword(com.enterprise.auth.application.dto.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = UUID.randomUUID().toString();
        tokenRepository.savePasswordResetToken(token, user.getEmail(), 3600000L); // 1 hour
        
        // Mock email sending
        System.out.println("Mock Email: Password reset token for " + user.getEmail() + " is " + token);
    }

    @Override
    public void resetPassword(com.enterprise.auth.application.dto.PasswordResetRequest request) {
        String email = tokenRepository.getEmailByPasswordResetToken(request.getToken());
        if (email == null) {
            throw new RuntimeException("Invalid or expired token");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        
        tokenRepository.deletePasswordResetToken(request.getToken());
        tokenRepository.revokeRefreshToken(user.getId().toString());
    }
}
