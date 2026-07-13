package com.kemp.dashboard.infrastructure.persistence;

import com.kemp.dashboard.domain.model.Dashboard;
import com.kemp.dashboard.domain.repository.DashboardRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardRepositoryAdapter implements DashboardRepository {
    
    private final DashboardJpaRepository repository;

    @Override
    public Dashboard save(Dashboard dashboard) {
        DashboardEntity entity = toEntity(dashboard);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Dashboard> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Optional<Dashboard> findByPublicToken(String publicToken) {
        return repository.findByPublicToken(publicToken).map(this::toDomain);
    }

    @Override
    public Page<Dashboard> findAllByTenantIdAndCreatedBy(UUID tenantId, UUID createdBy, Pageable pageable) {
        return repository.findAllByTenantIdAndCreatedByWithFilters(tenantId, createdBy, pageable).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
    
    private DashboardEntity toEntity(Dashboard domain) {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPublicToken(domain.getPublicToken());
        entity.setFullscreenEnabled(domain.getFullscreenEnabled());
        entity.setAutoRotate(domain.getAutoRotate());
        entity.setAutoRotateInterval(domain.getAutoRotateInterval());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setLayoutJson(domain.getLayoutJson());
        return entity;
    }
    
    private Dashboard toDomain(DashboardEntity entity) {
        return Dashboard.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .name(entity.getName())
            .description(entity.getDescription())
            .publicToken(entity.getPublicToken())
            .fullscreenEnabled(entity.getFullscreenEnabled())
            .autoRotate(entity.getAutoRotate())
            .autoRotateInterval(entity.getAutoRotateInterval())
            .createdBy(entity.getCreatedBy())
            .layoutJson(entity.getLayoutJson())
            .build();
    }
}
