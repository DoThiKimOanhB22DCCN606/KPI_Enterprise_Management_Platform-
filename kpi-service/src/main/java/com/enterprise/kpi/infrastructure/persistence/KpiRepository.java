package com.enterprise.kpi.infrastructure.persistence;

import com.enterprise.kpi.domain.model.KpiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KpiRepository extends JpaRepository<KpiEntity, UUID> {
    Optional<KpiEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    
    List<KpiEntity> findByStatusAndStartDateLessThanEqual(KpiStatus status, LocalDate date);
    List<KpiEntity> findByStatusAndEndDateBefore(KpiStatus status, LocalDate date);
    List<KpiEntity> findByStatusAndUpdatedAtBefore(KpiStatus status, Instant cutoff);

    org.springframework.data.domain.Page<KpiEntity> findByTenantId(UUID tenantId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<KpiEntity> findByTenantIdAndStatus(UUID tenantId, KpiStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<KpiEntity> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<KpiEntity> findByTenantIdAndStatusAndOwnerId(UUID tenantId, KpiStatus status, UUID ownerId, org.springframework.data.domain.Pageable pageable);
    
    List<KpiEntity> findByTenantIdAndCycleId(UUID tenantId, UUID cycleId);
}
