package com.enterprise.kpi.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EvaluationCycleRepository extends JpaRepository<EvaluationCycleEntity, UUID> {
    Optional<EvaluationCycleEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    java.util.List<EvaluationCycleEntity> findAllByTenantId(UUID tenantId);
}
