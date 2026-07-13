package com.kemp.organization.infrastructure.persistence;

import com.kemp.organization.domain.model.OrgUnitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgUnitJpaRepository extends JpaRepository<OrgUnitEntity, UUID> {
    
    Optional<OrgUnitEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    
    @Query("SELECT o FROM OrgUnitEntity o WHERE o.tenantId = :tenantId AND o.active = true " +
           "AND (:type IS NULL OR o.type = :type) " +
           "AND (CAST(:parentId AS uuid) IS NULL OR o.parentId = :parentId)")
    Page<OrgUnitEntity> findAllWithFilters(@Param("tenantId") UUID tenantId, 
                                           @Param("type") OrgUnitType type, 
                                           @Param("parentId") UUID parentId, 
                                           Pageable pageable);
                                           
    List<OrgUnitEntity> findByParentIdAndTenantIdAndActiveTrue(UUID parentId, UUID tenantId);
    
    @Query(value = "SELECT * FROM organization_units WHERE path <@ CAST(:path AS ltree) AND tenant_id = :tenantId AND active = true ORDER BY level", nativeQuery = true)
    List<OrgUnitEntity> findSubtree(@Param("path") String path, @Param("tenantId") UUID tenantId);
}
