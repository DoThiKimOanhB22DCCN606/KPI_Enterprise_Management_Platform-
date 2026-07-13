package com.kemp.user.domain.repository;

import com.kemp.user.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Optional<Role> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Role> findAllByIdAndTenantId(List<UUID> ids, UUID tenantId);
}
