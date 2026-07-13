package com.enterprise.goal.domain.repository;

import com.enterprise.goal.domain.model.Goal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository {
    List<Goal> findAllByParentGoalIdAndTenantId(UUID parentGoalId, UUID tenantId);
    List<Goal> findByKpiIdAndTenantId(UUID kpiId, UUID tenantId);
    Optional<Goal> findByIdAndTenantId(UUID id, UUID tenantId);
    org.springframework.data.domain.Page<Goal> findAllByTenantId(UUID tenantId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Goal> findFiltered(UUID tenantId, UUID ownerId, String ownerType, String status, org.springframework.data.domain.Pageable pageable);
    Goal save(Goal goal);
    void delete(Goal goal);
}
