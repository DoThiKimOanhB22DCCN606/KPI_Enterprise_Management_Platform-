package com.enterprise.goal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalJpaRepository extends JpaRepository<GoalEntity, UUID> {
    List<GoalEntity> findAllByParentGoalIdAndTenantId(UUID parentGoalId, UUID tenantId);
    List<GoalEntity> findByKpiIdAndTenantId(UUID kpiId, UUID tenantId);
    Optional<GoalEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    org.springframework.data.domain.Page<GoalEntity> findAllByTenantId(UUID tenantId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT g FROM GoalEntity g WHERE g.tenantId = :tenantId " +
        "AND (:ownerId IS NULL OR g.ownerId = :ownerId) " +
        "AND (:ownerType IS NULL OR g.ownerType = :ownerType) " +
        "AND (:status IS NULL OR g.status = :status)")
    org.springframework.data.domain.Page<GoalEntity> findFiltered(
        @org.springframework.data.repository.query.Param("tenantId") UUID tenantId,
        @org.springframework.data.repository.query.Param("ownerId") UUID ownerId,
        @org.springframework.data.repository.query.Param("ownerType") String ownerType,
        @org.springframework.data.repository.query.Param("status") String status,
        org.springframework.data.domain.Pageable pageable
    );
}
