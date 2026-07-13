package com.kemp.integration.infrastructure.persistence;

import com.kemp.integration.domain.model.WebhookSubscription;
import com.kemp.integration.domain.repository.WebhookRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookRepositoryAdapter implements WebhookRepository {

    private final WebhookJpaRepository repository;

    @Override
    public WebhookSubscription save(WebhookSubscription webhook) {
        WebhookEntity entity = toEntity(webhook);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<WebhookSubscription> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public List<WebhookSubscription> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private WebhookEntity toEntity(WebhookSubscription domain) {
        WebhookEntity entity = new WebhookEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setUrl(domain.getUrl());
        entity.setEvents(domain.getEvents());
        entity.setSecretHash(domain.getSecretHash());
        entity.setActive(domain.getActive());
        return entity;
    }

    private WebhookSubscription toDomain(WebhookEntity entity) {
        return WebhookSubscription.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .url(entity.getUrl())
            .events(entity.getEvents())
            .secretHash(entity.getSecretHash())
            .active(entity.getActive())
            .build();
    }
}
