package com.kemp.organization.infrastructure.persistence;

import com.kemp.organization.domain.model.OrgUnit;
import com.kemp.organization.domain.model.OrgUnitType;
import com.kemp.organization.domain.repository.OrgUnitRepository;
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
public class OrgUnitRepositoryAdapter implements OrgUnitRepository {
    
    private final OrgUnitJpaRepository repository;
    
    @Override
    public OrgUnit save(OrgUnit unit) {
        OrgUnitEntity entity = toEntity(unit);
        entity = repository.save(entity);
        return toDomain(entity);
    }
    
    @Override
    public Optional<OrgUnit> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }
    
    @Override
    public Page<OrgUnit> findAllByTenantId(UUID tenantId, OrgUnitType type, UUID parentId, Pageable pageable) {
        return repository.findAllWithFilters(tenantId, type, parentId, pageable).map(this::toDomain);
    }
    
    @Override
    public List<OrgUnit> findByParentIdAndTenantId(UUID parentId, UUID tenantId) {
        return repository.findByParentIdAndTenantIdAndActiveTrue(parentId, tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrgUnit> findSubtree(String path, UUID tenantId) {
        return repository.findSubtree(path, tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    private OrgUnitEntity toEntity(OrgUnit domain) {
        OrgUnitEntity entity = new OrgUnitEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setParentId(domain.getParentId());
        entity.setType(domain.getType());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setManagerUserId(domain.getManagerUserId());
        entity.setPath(domain.getPath());
        entity.setLevel(domain.getLevel());
        entity.setActive(domain.getActive() != null ? domain.getActive() : true);
        return entity;
    }
    
    private OrgUnit toDomain(OrgUnitEntity entity) {
        return OrgUnit.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .parentId(entity.getParentId())
            .type(entity.getType())
            .code(entity.getCode())
            .name(entity.getName())
            .managerUserId(entity.getManagerUserId())
            .path(entity.getPath())
            .level(entity.getLevel())
            .active(entity.getActive())
            .build();
    }
}
