package com.enterprise.auth.interfaces.rest;

import com.enterprise.auth.application.dto.AuthResponse;
import com.enterprise.auth.application.service.TotpService;
import com.enterprise.auth.domain.repository.TokenRepository;
import com.enterprise.auth.infrastructure.persistence.JpaUserRepository;
import com.enterprise.auth.infrastructure.persistence.UserEntity;
import com.enterprise.auth.infrastructure.security.JwtTokenProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final TotpService totpService;
    private final JpaUserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    @Data
    public static class MfaSetupResponse {
        private String secret;
        private String qrCodeUri;
    }

    @Data
    public static class MfaVerifyRequest {
        private UUID userId;
        private String tempToken;
        private String code;
    }

    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(@RequestParam UUID userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = userOpt.get();
        String secret = totpService.generateSecret();
        
        // Save the secret temporarily or directly (requires verification before enabling usually)
        user.setMfaSecret(secret);
        userRepository.save(user);

        MfaSetupResponse response = new MfaSetupResponse();
        response.setSecret(secret);
        response.setQrCodeUri(totpService.getQrCodeImageUri(secret, user.getEmail()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerifyRequest request) {
        UUID userId = request.getUserId();
        if (request.getTempToken() != null && !request.getTempToken().isEmpty()) {
            try {
                String userIdStr = jwtTokenProvider.getUserIdFromExpiredToken(request.getTempToken());
                userId = UUID.fromString(userIdStr);
            } catch (Exception e) {
                return ResponseEntity.status(401).body(java.util.Map.of("message", "Invalid or expired temporary token"));
            }
        }

        if (userId == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "User ID or Temporary Token is required"));
        }

        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity userEntity = userOpt.get();
        if (userEntity.getMfaSecret() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "MFA is not set up"));
        }

        boolean isValid = totpService.verifyCode(userEntity.getMfaSecret(), request.getCode());
        if (!isValid) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Invalid TOTP code"));
        }

        // Enable MFA for user if not already enabled
        if (!userEntity.isMfaEnabled()) {
            userEntity.setMfaEnabled(true);
            userRepository.save(userEntity);
        }

        // Fetch roles and generate final tokens
        java.util.List<String> roles = userRepository.findRoleCodesByUserId(userEntity.getId());
        com.enterprise.auth.domain.model.User user = com.enterprise.auth.domain.model.User.builder()
                .id(userEntity.getId())
                .tenantId(userEntity.getTenantId())
                .email(userEntity.getEmail())
                .passwordHash(userEntity.getPasswordHash())
                .fullName(userEntity.getFullName())
                .status(userEntity.getStatus())
                .roles(roles)
                .mfaEnabled(userEntity.isMfaEnabled())
                .mfaSecret(userEntity.getMfaSecret())
                .build();

        com.enterprise.auth.domain.model.AuthToken token = jwtTokenProvider.generateToken(user);
        
        tokenRepository.saveRefreshToken(user.getId().toString(), token.getRefreshToken(), 604800000L); // 7 days

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .expiresIn(token.getExpiresIn())
                .tokenType("Bearer")
                .mfaRequired(false)
                .build();

        return ResponseEntity.ok(authResponse);
    }
}
