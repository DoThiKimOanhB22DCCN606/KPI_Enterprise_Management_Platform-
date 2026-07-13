package com.kemp.integration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookJpaRepository extends JpaRepository<WebhookEntity, UUID> {
    Optional<WebhookEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<WebhookEntity> findAllByTenantId(UUID tenantId);
}
