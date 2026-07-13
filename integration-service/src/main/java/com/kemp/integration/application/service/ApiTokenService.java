package com.kemp.integration.application.service;

import com.kemp.integration.application.dto.ApiTokenResponse;
import com.kemp.integration.application.dto.CreateApiTokenRequest;
import com.kemp.integration.domain.model.ApiToken;
import com.kemp.integration.domain.repository.ApiTokenRepository;
import com.kemp.integration.infrastructure.config.TenantContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private final ApiTokenRepository apiTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiTokenResponse createToken(CreateApiTokenRequest request) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        
        ApiToken token = ApiToken.builder()
            .tenantId(getTenantId())
            .userId(getUserId())
            .name(request.getName())
            .tokenHash(passwordEncoder.encode(rawToken))
            .expiredAt(LocalDateTime.now().plusYears(1))
            .build();
            
        token = apiTokenRepository.save(token);
        
        return ApiTokenResponse.builder()
            .id(token.getId())
            .tenantId(token.getTenantId())
            .userId(token.getUserId())
            .name(token.getName())
            .expiredAt(token.getExpiredAt())
            .token(rawToken) // Returned exactly once
            .build();
    }

    public List<ApiTokenResponse> listTokens() {
        return apiTokenRepository.findAllByTenantId(getTenantId()).stream()
            .map(t -> ApiTokenResponse.builder()
                .id(t.getId())
                .tenantId(t.getTenantId())
                .userId(t.getUserId())
                .name(t.getName())
                .expiredAt(t.getExpiredAt())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void revokeToken(UUID id) {
        apiTokenRepository.findByIdAndTenantId(id, getTenantId())
            .ifPresent(t -> apiTokenRepository.deleteById(id));
    }

    public boolean validateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        
        List<ApiToken> tokens = apiTokenRepository.findAllByTenantId(getTenantId());
        for (ApiToken token : tokens) {
            if (passwordEncoder.matches(rawToken, token.getTokenHash())) {
                return token.getExpiredAt().isAfter(LocalDateTime.now());
            }
        }
        return false;
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new RuntimeException("TenantContext missing");
        return tenantId;
    }
    
    private UUID getUserId() {
        UUID userId = TenantContext.getUserId();
        if (userId == null) throw new RuntimeException("TenantContext missing");
        return userId;
    }
}
