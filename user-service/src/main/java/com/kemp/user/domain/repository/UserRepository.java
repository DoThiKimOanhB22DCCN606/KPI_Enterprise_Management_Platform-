package com.kemp.user.domain.repository;

import com.kemp.user.domain.model.User;
import com.kemp.user.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    User save(User user);
    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);
    Page<User> findAllByTenantId(UUID tenantId, User.Status status, String roleCode, Pageable pageable);
    void deleteRoles(UUID userId);
    void assignRole(UUID userId, UUID roleId);
    List<Role> findRolesByUserId(UUID userId);
}
