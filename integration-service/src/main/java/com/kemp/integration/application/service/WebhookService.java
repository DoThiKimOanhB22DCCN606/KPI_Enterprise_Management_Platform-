package com.kemp.integration.application.service;

import com.kemp.integration.application.dto.CreateWebhookRequest;
import com.kemp.integration.application.dto.WebhookResponse;
import com.kemp.integration.domain.model.WebhookSubscription;
import com.kemp.integration.domain.repository.WebhookRepository;
import com.kemp.integration.infrastructure.config.TenantContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookRepository webhookRepository;

    @Transactional
    public WebhookResponse createWebhook(CreateWebhookRequest request) {
        WebhookSubscription webhook = WebhookSubscription.builder()
            .tenantId(getTenantId())
            .url(request.getUrl())
            .events(request.getEvents())
            .secretHash(UUID.randomUUID().toString()) // Should be secure generated secret
            .active(true)
            .build();
            
        webhook = webhookRepository.save(webhook);
        return toResponse(webhook);
    }

    public List<WebhookResponse> listWebhooks() {
        return webhookRepository.findAllByTenantId(getTenantId()).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteWebhook(UUID id) {
        webhookRepository.findByIdAndTenantId(id, getTenantId())
            .ifPresent(w -> webhookRepository.deleteById(id));
    }

    private WebhookResponse toResponse(WebhookSubscription webhook) {
        return WebhookResponse.builder()
            .id(webhook.getId())
            .tenantId(webhook.getTenantId())
            .url(webhook.getUrl())
            .events(webhook.getEvents())
            .active(webhook.getActive())
            .build();
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new RuntimeException("TenantContext missing");
        return tenantId;
    }
}
