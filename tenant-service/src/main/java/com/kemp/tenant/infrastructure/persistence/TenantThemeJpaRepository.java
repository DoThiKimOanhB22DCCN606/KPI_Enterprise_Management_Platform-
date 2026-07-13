package com.kemp.tenant.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantThemeJpaRepository extends JpaRepository<TenantThemeEntity, UUID> {
    Optional<TenantThemeEntity> findByTenantId(UUID tenantId);
}
