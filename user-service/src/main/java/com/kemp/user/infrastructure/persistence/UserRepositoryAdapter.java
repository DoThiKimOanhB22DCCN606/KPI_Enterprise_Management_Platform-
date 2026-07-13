package com.kemp.user.infrastructure.persistence;

import com.kemp.user.domain.model.Role;
import com.kemp.user.domain.model.User;
import com.kemp.user.domain.repository.RoleRepository;
import com.kemp.user.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId() == null ? UUID.randomUUID() : user.getId());
        entity.setTenantId(user.getTenantId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFullName(user.getFullName());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setPhone(user.getPhone());
        entity.setEmployeeCode(user.getEmployeeCode());
        entity.setStatus(user.getStatus());
        entity.setLastLoginAt(user.getLastLoginAt());
        entity.setOrganizationUnitId(user.getOrganizationUnitId());
        entity.setMfaSecret(user.getMfaSecret());
        entity.setMfaEnabled(user.getMfaEnabled() != null ? user.getMfaEnabled() : false);
        entity.setCreatedAt(user.getCreatedAt());
        entity.setCreatedBy(user.getCreatedBy());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setUpdatedBy(user.getUpdatedBy());
        entity.setVersion(user.getVersion());
        entity = userJpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<User> findByIdAndTenantId(UUID id, UUID tenantId) {
        return userJpaRepository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmailAndTenantId(String email, UUID tenantId) {
        return userJpaRepository.findByEmailAndTenantId(email, tenantId).map(this::toDomain);
    }

    @Override
    public Page<User> findAllByTenantId(UUID tenantId, User.Status status, String roleCode, Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        return userJpaRepository.findAllByTenantIdWithFilters(tenantId, statusStr, roleCode, pageable)
                .map(this::toDomain);
    }

    @Override
    public void deleteRoles(UUID userId) {
        userJpaRepository.deleteRoles(userId);
    }

    @Override
    public void assignRole(UUID userId, UUID roleId) {
        userJpaRepository.assignRole(userId, roleId);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<Role> findRolesByUserId(UUID userId) {
        String sql = "SELECT * FROM roles WHERE id IN (SELECT role_id FROM user_roles WHERE user_id = :userId)";
        List<RoleEntity> entities = entityManager.createNativeQuery(sql, RoleEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        return entities.stream().map(this::toRoleDomain).collect(Collectors.toList());
    }



    private User toDomain(UserEntity entity) {
        return User.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .email(entity.getEmail())
            .passwordHash(entity.getPasswordHash())
            .fullName(entity.getFullName())
            .avatarUrl(entity.getAvatarUrl())
            .phone(entity.getPhone())
            .employeeCode(entity.getEmployeeCode())
            .status(entity.getStatus())
            .lastLoginAt(entity.getLastLoginAt())
            .organizationUnitId(entity.getOrganizationUnitId())
            .mfaSecret(entity.getMfaSecret())
            .mfaEnabled(entity.getMfaEnabled())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .version(entity.getVersion())
            .build();
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
