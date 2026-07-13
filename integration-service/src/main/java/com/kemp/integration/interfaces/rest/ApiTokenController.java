package com.kemp.integration.interfaces.rest;

import com.kemp.integration.application.dto.ApiTokenResponse;
import com.kemp.integration.application.dto.CreateApiTokenRequest;
import com.kemp.integration.application.service.ApiTokenService;
import com.kemp.integration.infrastructure.config.TenantContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/integrations/api-tokens")
@RequiredArgsConstructor
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    @ModelAttribute
    public void setMockTenantContext(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (tenantId != null) {
            TenantContext.setTenantId(UUID.fromString(tenantId));
        } else {
            TenantContext.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }
        
        if (userId != null) {
            TenantContext.setUserId(UUID.fromString(userId));
        } else {
            TenantContext.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        }
    }

    @GetMapping
    public ResponseEntity<List<ApiTokenResponse>> listTokens() {
        return ResponseEntity.ok(apiTokenService.listTokens());
    }

    @PostMapping
    public ResponseEntity<ApiTokenResponse> createToken(@Valid @RequestBody CreateApiTokenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiTokenService.createToken(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeToken(@PathVariable UUID id) {
        apiTokenService.revokeToken(id);
        return ResponseEntity.noContent().build();
    }
}
