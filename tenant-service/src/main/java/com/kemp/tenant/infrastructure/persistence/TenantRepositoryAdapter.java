package com.kemp.tenant.infrastructure.persistence;

import com.kemp.tenant.domain.model.Tenant;
import com.kemp.tenant.domain.repository.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantRepositoryAdapter implements TenantRepository {
    
    private final TenantJpaRepository tenantRepo;
    
    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity entity = new TenantEntity();
        entity.setId(tenant.getId() == null ? UUID.randomUUID() : tenant.getId());
        entity.setCode(tenant.getCode());
        entity.setName(tenant.getName());
        entity.setLogoUrl(tenant.getLogoUrl());
        entity.setStatus(tenant.getStatus());
        entity.setTimezone(tenant.getTimezone());
        entity.setVersion(tenant.getVersion());
        entity = tenantRepo.save(entity);
        tenant.setId(entity.getId());
        return tenant;
    }
    
    @Override
    public Optional<Tenant> findById(UUID id) {
        return tenantRepo.findById(id).map(this::toTenantDomain);
    }
    
    @Override
    public Optional<Tenant> findByCode(String code) {
        return tenantRepo.findByCode(code).map(this::toTenantDomain);
    }



    private Tenant toTenantDomain(TenantEntity entity) {
        return Tenant.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .logoUrl(entity.getLogoUrl())
            .status(entity.getStatus())
            .timezone(entity.getTimezone())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .version(entity.getVersion())
            .build();
    }
}
