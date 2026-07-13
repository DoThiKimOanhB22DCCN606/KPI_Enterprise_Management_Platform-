package com.kemp.user.infrastructure.persistence;

import com.kemp.user.domain.model.Role;
import com.kemp.user.domain.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;

    @Override
    public Optional<Role> findByIdAndTenantId(UUID id, UUID tenantId) {
        return roleJpaRepository.findByIdAndTenantId(id, tenantId).map(this::toRoleDomain);
    }

    @Override
    public List<Role> findAllByIdAndTenantId(List<UUID> ids, UUID tenantId) {
        return roleJpaRepository.findAllByIdInAndTenantId(ids, tenantId).stream()
                .map(this::toRoleDomain)
                .collect(Collectors.toList());
    }

    private Role toRoleDomain(RoleEntity entity) {
        return Role.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .code(entity.getCode())
            .name(entity.getName())
            .description(entity.getDescription())
            .systemRole(entity.getSystemRole())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .version(entity.getVersion())
            .build();
    }
}
