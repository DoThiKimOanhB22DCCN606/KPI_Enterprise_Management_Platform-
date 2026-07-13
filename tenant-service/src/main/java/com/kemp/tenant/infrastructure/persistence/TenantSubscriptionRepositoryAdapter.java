package com.kemp.tenant.infrastructure.persistence;

import com.kemp.tenant.domain.model.TenantSubscription;
import com.kemp.tenant.domain.repository.TenantSubscriptionRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantSubscriptionRepositoryAdapter implements TenantSubscriptionRepository {
    
    private final TenantSubscriptionJpaRepository subRepo;

    @Override
    public TenantSubscription save(TenantSubscription sub) {
        TenantSubscriptionEntity entity = new TenantSubscriptionEntity();
        entity.setId(sub.getId() == null ? UUID.randomUUID() : sub.getId());
        entity.setTenantId(sub.getTenantId());
        entity.setPlanType(sub.getPlanType());
        entity.setMaxUsers(sub.getMaxUsers());
        entity.setMaxKpis(sub.getMaxKpis());
        entity.setExpiresAt(sub.getExpiresAt());
        entity = subRepo.save(entity);
        sub.setId(entity.getId());
        return sub;
    }

    @Override
    public Optional<TenantSubscription> findByTenantId(UUID tenantId) {
        return subRepo.findByTenantId(tenantId).map(this::toSubDomain);
    }

    private TenantSubscription toSubDomain(TenantSubscriptionEntity entity) {
        return TenantSubscription.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .planType(entity.getPlanType())
            .maxUsers(entity.getMaxUsers())
            .maxKpis(entity.getMaxKpis())
            .expiresAt(entity.getExpiresAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
