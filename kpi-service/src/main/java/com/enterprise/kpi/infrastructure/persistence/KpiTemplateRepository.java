package com.enterprise.kpi.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KpiTemplateRepository extends JpaRepository<KpiTemplateEntity, UUID> {
    List<KpiTemplateEntity> findAllByTenantIdAndCategory(UUID tenantId, String category);
    List<KpiTemplateEntity> findAllByTenantId(UUID tenantId);
    Optional<KpiTemplateEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
