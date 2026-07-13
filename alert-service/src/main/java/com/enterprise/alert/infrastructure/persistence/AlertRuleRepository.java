package com.enterprise.alert.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRuleEntity, UUID> {

    Page<AlertRuleEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Page<AlertRuleEntity> findByTenantIdAndEnabled(UUID tenantId, boolean enabled, Pageable pageable);
}
