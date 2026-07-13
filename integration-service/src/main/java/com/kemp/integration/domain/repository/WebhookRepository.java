package com.kemp.integration.domain.repository;

import com.kemp.integration.domain.model.WebhookSubscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookRepository {
    WebhookSubscription save(WebhookSubscription webhook);
    Optional<WebhookSubscription> findByIdAndTenantId(UUID id, UUID tenantId);
    List<WebhookSubscription> findAllByTenantId(UUID tenantId);
    void deleteById(UUID id);
}
