package com.kemp.integration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ImportJobJpaRepository extends JpaRepository<ImportJobEntity, UUID> {
    Optional<ImportJobEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
