package com.kemp.organization.application.service;

import com.kemp.organization.application.dto.CreateOrgUnitRequest;
import com.kemp.organization.application.dto.UpdateOrgUnitRequest;
import com.kemp.organization.domain.model.OrgUnit;
import com.kemp.organization.domain.model.OrgUnitType;
import com.kemp.organization.domain.repository.OrgUnitRepository;
import com.kemp.organization.infrastructure.config.TenantContext;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrgUnitService {

    private final OrgUnitRepository orgUnitRepository;

    @Transactional
    public OrgUnit createUnit(CreateOrgUnitRequest request) {
        UUID tenantId = getTenantId();
        UUID newId = UUID.randomUUID();
        
        String path;
        Integer level;
        
        if (request.getParentId() == null) {
            path = "root_" + tenantId.toString().replace("-", "");
            level = 0;
        } else {
            OrgUnit parent = getUnit(request.getParentId());
            path = parent.getPath() + "." + newId.toString().replace("-", "_");
            level = parent.getLevel() + 1;
        }
        
        OrgUnit unit = OrgUnit.builder()
            .id(newId)
            .tenantId(tenantId)
            .parentId(request.getParentId())
            .type(request.getType())
            .code(request.getCode())
            .name(request.getName())
            .managerUserId(request.getManagerUserId())
            .path(path)
            .level(level)
            .active(true)
            .build();
            
        return orgUnitRepository.save(unit);
    }

    public Page<OrgUnit> listUnits(OrgUnitType type, UUID parentId, Pageable pageable) {
        return orgUnitRepository.findAllByTenantId(getTenantId(), type, parentId, pageable);
    }

    public OrgUnit getUnit(UUID id) {
        return orgUnitRepository.findByIdAndTenantId(id, getTenantId())
                .filter(OrgUnit::getActive)
                .orElseThrow(() -> new RuntimeException("Organization Unit not found"));
    }

    public List<OrgUnit> getSubtree(UUID id) {
        OrgUnit root = getUnit(id);
        return orgUnitRepository.findSubtree(root.getPath(), getTenantId());
    }

    @Transactional
    public OrgUnit updateUnit(UUID id, UpdateOrgUnitRequest request) {
        OrgUnit unit = getUnit(id);
        unit.setName(request.getName());
        unit.setManagerUserId(request.getManagerUserId());
        return orgUnitRepository.save(unit);
    }

    @Transactional
    public OrgUnit moveUnit(UUID id, UUID newParentId) {
        OrgUnit unit = getUnit(id);
        
        if (newParentId != null && newParentId.equals(id)) {
            throw new IllegalArgumentException("Cannot set organization unit as its own parent");
        }
        
        OrgUnit newParent = null;
        if (newParentId != null) {
            newParent = getUnit(newParentId);
            
            if (newParent.getPath().startsWith(unit.getPath() + ".") || newParent.getPath().equals(unit.getPath())) {
                throw new IllegalArgumentException("Cannot move an organization unit to its own descendant");
            }
        }
        
        String oldPathPrefix = unit.getPath();
        String newPath;
        int levelDifference;
        
        if (newParent == null) {
            newPath = "root_" + unit.getTenantId().toString().replace("-", "");
            levelDifference = 0 - unit.getLevel();
            unit.setParentId(null);
        } else {
            newPath = newParent.getPath() + "." + unit.getId().toString().replace("-", "_");
            levelDifference = (newParent.getLevel() + 1) - unit.getLevel();
            unit.setParentId(newParentId);
        }
        
        unit.setPath(newPath);
        unit.setLevel(unit.getLevel() + levelDifference);
        orgUnitRepository.save(unit);
        
        List<OrgUnit> descendants = orgUnitRepository.findSubtree(oldPathPrefix, getTenantId());
        for (OrgUnit desc : descendants) {
            if (desc.getId().equals(unit.getId())) continue;
            
            String updatedPath = newPath + desc.getPath().substring(oldPathPrefix.length());
            desc.setPath(updatedPath);
            desc.setLevel(desc.getLevel() + levelDifference);
            orgUnitRepository.save(desc);
        }
        
        return unit;
    }

    @Transactional
    public OrgUnit softDelete(UUID id) {
        OrgUnit unit = getUnit(id);
        unit.setActive(false);
        return orgUnitRepository.save(unit);
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("TenantContext is empty");
        }
        return tenantId;
    }
}
