package com.kemp.organization.domain.repository;

import com.kemp.organization.domain.model.OrgUnit;
import com.kemp.organization.domain.model.OrgUnitType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrgUnitRepository {
    OrgUnit save(OrgUnit unit);
    Optional<OrgUnit> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<OrgUnit> findAllByTenantId(UUID tenantId, OrgUnitType type, UUID parentId, Pageable pageable);
    List<OrgUnit> findByParentIdAndTenantId(UUID parentId, UUID tenantId);
    List<OrgUnit> findSubtree(String path, UUID tenantId);
}
