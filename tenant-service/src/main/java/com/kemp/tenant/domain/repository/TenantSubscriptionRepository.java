package com.kemp.tenant.domain.repository;

import com.kemp.tenant.domain.model.TenantSubscription;
import java.util.Optional;
import java.util.UUID;

public interface TenantSubscriptionRepository {
    TenantSubscription save(TenantSubscription subscription);
    Optional<TenantSubscription> findByTenantId(UUID tenantId);
}
