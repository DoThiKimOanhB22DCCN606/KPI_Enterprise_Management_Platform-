package com.kemp.user.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<UserEntity> findByEmailAndTenantId(String email, UUID tenantId);
    
    @Query(value = "SELECT DISTINCT u.* FROM users u " +
           "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
           "LEFT JOIN roles r ON ur.role_id = r.id " +
           "WHERE u.tenant_id = :tenantId " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:roleCode IS NULL OR r.code = :roleCode)", 
           countQuery = "SELECT count(DISTINCT u.id) FROM users u " +
           "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
           "LEFT JOIN roles r ON ur.role_id = r.id " +
           "WHERE u.tenant_id = :tenantId " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:roleCode IS NULL OR r.code = :roleCode)",
           nativeQuery = true)
    Page<UserEntity> findAllByTenantIdWithFilters(
        @Param("tenantId") UUID tenantId, 
        @Param("status") String status, 
        @Param("roleCode") String roleCode, 
        Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId", nativeQuery = true)
    void deleteRoles(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_roles (user_id, role_id, assigned_at) VALUES (:userId, :roleId, NOW()) ON CONFLICT DO NOTHING", nativeQuery = true)
    void assignRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Query(value = "SELECT * FROM roles WHERE id IN (SELECT role_id FROM user_roles WHERE user_id = :userId)", nativeQuery = true)
    List<RoleEntity> findRolesByUserId(@Param("userId") UUID userId);
}
