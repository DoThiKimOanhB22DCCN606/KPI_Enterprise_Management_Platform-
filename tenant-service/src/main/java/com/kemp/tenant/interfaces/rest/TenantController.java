package com.kemp.tenant.interfaces.rest;

import com.kemp.tenant.application.dto.CreateTenantRequest;
import com.kemp.tenant.application.dto.TenantResponse;
import com.kemp.tenant.application.dto.UpdateSubscriptionRequest;
import com.kemp.tenant.application.dto.UpdateTenantRequest;
import com.kemp.tenant.application.dto.UpdateThemeRequest;
import com.kemp.tenant.application.service.TenantService;
import com.kemp.tenant.domain.model.Tenant;
import com.kemp.tenant.domain.model.TenantSubscription;
import com.kemp.tenant.domain.model.TenantTheme;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(tenant));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable UUID id) {
        Tenant tenant = tenantService.getTenant(id);
        return ResponseEntity.ok(toResponse(tenant));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(@PathVariable UUID id, @Valid @RequestBody UpdateTenantRequest request) {
        Tenant tenant = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(toResponse(tenant));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<TenantResponse> deactivateTenant(@PathVariable UUID id) {
        Tenant tenant = tenantService.deactivateTenant(id);
        return ResponseEntity.ok(toResponse(tenant));
    }

    @GetMapping("/{id}/subscription")
    public ResponseEntity<TenantSubscription> getSubscription(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getSubscription(id));
    }

    @PutMapping("/{id}/subscription")
    public ResponseEntity<TenantSubscription> updateSubscription(@PathVariable UUID id, @Valid @RequestBody UpdateSubscriptionRequest request) {
        return ResponseEntity.ok(tenantService.updateSubscription(id, request));
    }

    @GetMapping("/{id}/theme")
    public ResponseEntity<TenantTheme> getTheme(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getTheme(id));
    }

    @PutMapping("/{id}/theme")
    public ResponseEntity<TenantTheme> updateTheme(@PathVariable UUID id, @RequestBody UpdateThemeRequest request) {
        return ResponseEntity.ok(tenantService.updateTheme(id, request));
    }

    @PostMapping("/{id}/theme/logo")
    public ResponseEntity<Map<String, String>> uploadLogo(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        String url = tenantService.uploadLogo(id, file);
        return ResponseEntity.ok(Map.of("logo_url", url));
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
            .id(tenant.getId())
            .code(tenant.getCode())
            .name(tenant.getName())
            .logoUrl(tenant.getLogoUrl())
            .status(tenant.getStatus())
            .timezone(tenant.getTimezone())
            .createdAt(tenant.getCreatedAt())
            .build();
    }
}
