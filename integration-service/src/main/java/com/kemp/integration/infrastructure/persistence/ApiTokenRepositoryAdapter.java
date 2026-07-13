package com.kemp.integration.infrastructure.persistence;

import com.kemp.integration.domain.model.ApiToken;
import com.kemp.integration.domain.repository.ApiTokenRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiTokenRepositoryAdapter implements ApiTokenRepository {

    private final ApiTokenJpaRepository repository;

    @Override
    public ApiToken save(ApiToken token) {
        ApiTokenEntity entity = toEntity(token);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<ApiToken> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public List<ApiToken> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private ApiTokenEntity toEntity(ApiToken domain) {
        ApiTokenEntity entity = new ApiTokenEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setUserId(domain.getUserId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setName(domain.getName());
        entity.setExpiredAt(domain.getExpiredAt());
        return entity;
    }

    private ApiToken toDomain(ApiTokenEntity entity) {
        return ApiToken.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .userId(entity.getUserId())
            .tokenHash(entity.getTokenHash())
            .name(entity.getName())
            .expiredAt(entity.getExpiredAt())
            .build();
    }
}
