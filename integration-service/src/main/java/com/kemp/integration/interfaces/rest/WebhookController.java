package com.kemp.integration.interfaces.rest;

import com.kemp.integration.application.dto.CreateWebhookRequest;
import com.kemp.integration.application.dto.WebhookResponse;
import com.kemp.integration.application.service.WebhookService;
import com.kemp.integration.infrastructure.config.TenantContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/integrations/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @ModelAttribute
    public void setMockTenantContext(@RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        if (tenantId != null) {
            TenantContext.setTenantId(UUID.fromString(tenantId));
        } else {
            TenantContext.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }
    }

    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(@Valid @RequestBody CreateWebhookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(webhookService.createWebhook(request));
    }

    @GetMapping
    public ResponseEntity<List<WebhookResponse>> listWebhooks() {
        return ResponseEntity.ok(webhookService.listWebhooks());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(@PathVariable UUID id) {
        webhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }
}
