package com.enterprise.alert.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertInstanceRepository extends JpaRepository<AlertInstanceEntity, UUID> {

    Page<AlertInstanceEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Page<AlertInstanceEntity> findByTenantIdAndResolved(UUID tenantId, boolean resolved, Pageable pageable);

    Page<AlertInstanceEntity> findByTenantIdAndRuleId(UUID tenantId, UUID ruleId, Pageable pageable);
}
