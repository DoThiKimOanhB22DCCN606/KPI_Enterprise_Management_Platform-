package com.kemp.dashboard.domain.repository;

import com.kemp.dashboard.domain.model.Dashboard;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DashboardRepository {
    Dashboard save(Dashboard dashboard);
    Optional<Dashboard> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Dashboard> findByPublicToken(String publicToken);
    Page<Dashboard> findAllByTenantIdAndCreatedBy(UUID tenantId, UUID createdBy, Pageable pageable);
    void deleteById(UUID id);
}
