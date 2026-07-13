package com.kemp.integration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenJpaRepository extends JpaRepository<ApiTokenEntity, UUID> {
    Optional<ApiTokenEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<ApiTokenEntity> findAllByTenantId(UUID tenantId);
}
