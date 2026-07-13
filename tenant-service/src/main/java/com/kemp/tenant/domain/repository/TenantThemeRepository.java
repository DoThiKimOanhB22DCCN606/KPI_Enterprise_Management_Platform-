package com.kemp.tenant.domain.repository;

import com.kemp.tenant.domain.model.TenantTheme;
import java.util.Optional;
import java.util.UUID;

public interface TenantThemeRepository {
    TenantTheme save(TenantTheme theme);
    Optional<TenantTheme> findByTenantId(UUID tenantId);
}
