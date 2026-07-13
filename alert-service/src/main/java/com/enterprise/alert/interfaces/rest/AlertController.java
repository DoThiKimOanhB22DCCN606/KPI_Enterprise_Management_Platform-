package com.enterprise.alert.interfaces.rest;

import com.enterprise.alert.infrastructure.config.TenantContext;
import com.enterprise.alert.infrastructure.persistence.AlertInstanceEntity;
import com.enterprise.alert.infrastructure.persistence.AlertInstanceRepository;
import com.enterprise.alert.infrastructure.persistence.AlertRuleEntity;
import com.enterprise.alert.infrastructure.persistence.AlertRuleRepository;
import com.enterprise.alert.infrastructure.messaging.AlertPublisher;
import com.enterprise.alert.domain.event.AlertResolvedEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

/**
 * REST controller exposing alert management endpoints.
 *
 * TenantId is resolved from the JWT via {@link TenantContext} (set by JwtAuthenticationFilter).
 * The authenticated user's UUID principal is used as createdBy.
 *
 * Endpoints:
 *   GET  /v1/alerts           – paginated fired alert instances for current tenant
 *   GET  /v1/alert-rules      – paginated alert rules for current tenant
 *   POST /v1/alert-rules      – create a new alert rule
 *   DELETE /v1/alert-rules/{id} – disable (soft-delete) an alert rule
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AlertController {

    private final AlertRuleRepository     alertRuleRepository;
    private final AlertInstanceRepository alertInstanceRepository;
    private final AlertPublisher          alertPublisher;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /v1/alerts — Fired alert instances for current tenant
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/v1/alerts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AlertInstanceEntity>> getAlerts(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(required = false)      Boolean resolved
    ) {
        UUID tenantId = requireTenantId();
        int cappedSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, cappedSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AlertInstanceEntity> result = (resolved != null)
                ? alertInstanceRepository.findByTenantIdAndResolved(tenantId, resolved, pageable)
                : alertInstanceRepository.findByTenantId(tenantId, pageable);

        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /v1/alerts/{id}/resolve — Mark an alert as resolved
    // ─────────────────────────────────────────────────────────────────────────
    
    @PostMapping("/v1/alerts/{id}/resolve")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SYSTEM_ADMIN') or hasRole('TEAM_LEADER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> resolveAlert(@PathVariable UUID id) {
        UUID tenantId = requireTenantId();
        
        AlertInstanceEntity alert = alertInstanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert instance not found: " + id));
                
        if (!alert.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Alert does not belong to your tenant");
        }
        
        if (alert.isResolved()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Alert is already resolved"));
        }
        
        alert.setResolved(true);
        alert.setResolvedAt(OffsetDateTime.now());
        alertInstanceRepository.save(alert);
        
        AlertResolvedEvent event = AlertResolvedEvent.builder()
            .eventId(UUID.randomUUID())
            .alertId(alert.getId())
            .tenantId(tenantId)
            .ruleId(alert.getRuleId())
            .resolvedAt(alert.getResolvedAt())
            .build();
            
        alertPublisher.publishAlertResolved(event);
        
        return ResponseEntity.ok(Map.of(
            "id", id,
            "resolved", true,
            "message", "Alert resolved successfully"
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /v1/alert-rules — Alert rules for current tenant
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/v1/alert-rules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AlertRuleEntity>> getAlertRules(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(required = false)      Boolean enabledOnly
    ) {
        UUID tenantId = requireTenantId();
        int cappedSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, cappedSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AlertRuleEntity> result = Boolean.TRUE.equals(enabledOnly)
                ? alertRuleRepository.findByTenantIdAndEnabled(tenantId, true, pageable)
                : alertRuleRepository.findByTenantId(tenantId, pageable);

        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /v1/alert-rules — Create a new alert rule
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/v1/alert-rules")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AlertRuleEntity> createAlertRule(
            @Valid @RequestBody CreateAlertRuleRequest request
    ) {
        UUID tenantId  = requireTenantId();
        UUID createdBy = currentUserId();

        AlertRuleEntity entity = AlertRuleEntity.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .kpiId(request.getKpiId())
                .conditionType(request.getConditionType())
                .thresholdValue(request.getThresholdValue())
                .comparisonOperator(request.getComparisonOperator() != null
                        ? request.getComparisonOperator() : "LESS_THAN")
                .severity(request.getSeverity() != null
                        ? request.getSeverity() : "WARNING")
                .notificationChannel(request.getNotificationChannel() != null
                        ? request.getNotificationChannel() : "IN_APP")
                .enabled(true)
                .createdBy(createdBy)
                .build();

        AlertRuleEntity saved = alertRuleRepository.save(entity);
        log.info("Created alert rule [{}] '{}' for tenant {}", saved.getId(), saved.getName(), tenantId);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /v1/alert-rules/{id} — Soft-delete (disable) an alert rule
    // ─────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/v1/alert-rules/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAlertRule(@PathVariable UUID id) {
        UUID tenantId = requireTenantId();

        AlertRuleEntity rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Alert rule not found: " + id));

        // Enforce tenant isolation — rule must belong to the caller's tenant
        if (!rule.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Alert rule does not belong to your tenant");
        }

        // Soft-delete: disable the rule so history is preserved
        rule.setEnabled(false);
        alertRuleRepository.save(rule);
        log.info("Disabled alert rule [{}] for tenant {}", id, tenantId);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "enabled", false,
                "message", "Alert rule disabled successfully"
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private UUID requireTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Tenant context not available — missing or invalid JWT");
        }
        return tenantId;
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID) {
            return (UUID) auth.getPrincipal();
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Request DTO (inline — small enough not to need a separate file)
    // ─────────────────────────────────────────────────────────────────────────

    @Data
    public static class CreateAlertRuleRequest {

        @NotBlank
        private String name;

        private String description;

        /** UUID of the KPI to monitor */
        private UUID kpiId;

        /** e.g. "THRESHOLD" */
        private String conditionType;

        private BigDecimal thresholdValue;

        /** LESS_THAN | GREATER_THAN | EQUALS — defaults to LESS_THAN */
        private String comparisonOperator;

        /** INFO | WARNING | CRITICAL — defaults to WARNING */
        private String severity;

        /** IN_APP | EMAIL | SLACK | TELEGRAM — defaults to IN_APP */
        private String notificationChannel;
    }
}
