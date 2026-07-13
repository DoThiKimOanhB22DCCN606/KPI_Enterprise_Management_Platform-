package com.kemp.dashboard.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface DashboardJpaRepository extends JpaRepository<DashboardEntity, UUID> {
    Optional<DashboardEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<DashboardEntity> findByPublicToken(String publicToken);
    
    @Query("SELECT d FROM DashboardEntity d WHERE d.tenantId = :tenantId AND (:createdBy IS NULL OR d.createdBy = :createdBy)")
    Page<DashboardEntity> findAllByTenantIdAndCreatedByWithFilters(@Param("tenantId") UUID tenantId, @Param("createdBy") UUID createdBy, Pageable pageable);
}
