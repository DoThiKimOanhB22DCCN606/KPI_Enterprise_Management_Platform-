package com.kemp.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<RoleEntity> findAllByIdInAndTenantId(List<UUID> ids, UUID tenantId);
}
