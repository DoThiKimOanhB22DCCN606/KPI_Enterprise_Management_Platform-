package com.enterprise.auth.infrastructure.persistence;

import com.enterprise.auth.domain.model.User;
import com.enterprise.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(java.util.UUID id) {
        return jpaUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void save(User user) {
        UserEntity entity = jpaUserRepository.findById(user.getId()).orElseThrow();
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLockedUntil(user.getLockedUntil());
        jpaUserRepository.save(entity);
    }

    private User toDomain(UserEntity entity) {
        java.util.List<String> roles = jpaUserRepository.findRoleCodesByUserId(entity.getId());
        return User.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .fullName(entity.getFullName())
                .status(entity.getStatus())
                .roles(roles)
                .mfaEnabled(entity.isMfaEnabled())
                .mfaSecret(entity.getMfaSecret())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .lockedUntil(entity.getLockedUntil())
                .build();
    }
}
