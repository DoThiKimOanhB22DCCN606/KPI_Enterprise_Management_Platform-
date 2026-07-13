package com.kemp.integration.domain.repository;

import com.kemp.integration.domain.model.ApiToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenRepository {
    ApiToken save(ApiToken token);
    Optional<ApiToken> findByIdAndTenantId(UUID id, UUID tenantId);
    List<ApiToken> findAllByTenantId(UUID tenantId);
    void deleteById(UUID id);
}
