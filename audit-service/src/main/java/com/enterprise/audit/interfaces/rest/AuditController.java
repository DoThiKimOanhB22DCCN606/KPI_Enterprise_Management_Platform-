package com.enterprise.audit.interfaces.rest;

import com.enterprise.audit.application.dto.AuditLogResponse;
import com.enterprise.audit.infrastructure.config.TenantContext;
import com.enterprise.audit.infrastructure.persistence.AuditLogEntity;
import com.enterprise.audit.infrastructure.persistence.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * REST controller exposing audit log query endpoints.
 *
 * Security: requires authenticated user; TENANT_ADMIN role enforced via @PreAuthorize.
 * TenantId is resolved from the JWT via {@link TenantContext} (set by JwtAuthenticationFilter).
 *
 * Note on schema: AuditLogEntity was originally designed for internal event sourcing and
 * does not store tenantId. The entity fields are:
 *   eventType  → exposed as "action"
 *   actorId    → exposed as "userId"
 *   resourceId → exposed as "resourceId"
 *   oldVal     → exposed as "oldData"
 *   newVal     → exposed as "newData"
 *   timestamp  → exposed as "createdAt"
 *
 *
 * Tenant filtering is now enforced on all queries using the TenantContext resolved from the JWT/Gateway.
 */
@Slf4j
@RestController
@RequestMapping("/v1/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    /**
     * GET /v1/audit-logs
     *
     * Returns a paginated list of audit log entries.
     *
     * @param page      zero-based page index (default 0)
     * @param size      number of records per page (default 20, max 100)
     * @param action    optional filter by event type (e.g. "USER_LOGIN", "KPI_UPDATED")
     * @param userId    optional filter by actor/user ID string
     * @param startDate optional lower bound date (inclusive), ISO date format
     * @param endDate   optional upper bound date (inclusive), ISO date format
     */
    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String action,
            @RequestParam(required = false)    String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Cap page size to prevent abuse
        int cappedSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, cappedSize,
                Sort.by(Sort.Direction.DESC, "timestamp"));

        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }

        Page<AuditLogEntity> entityPage;
        UUID actorUuid = (userId != null && !userId.trim().isEmpty()) ? UUID.fromString(userId) : null;

        if (action != null && actorUuid != null) {
            entityPage = auditLogRepository.findByTenantIdAndEventTypeAndActorId(tenantId, action, actorUuid, pageable);
        } else if (action != null) {
            entityPage = auditLogRepository.findByTenantIdAndEventType(tenantId, action, pageable);
        } else if (actorUuid != null) {
            entityPage = auditLogRepository.findByTenantIdAndActorId(tenantId, actorUuid, pageable);
        } else if (startDate != null && endDate != null) {
            OffsetDateTime from = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime to   = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
            entityPage = auditLogRepository.findByTenantIdAndTimestampBetween(tenantId, from, to, pageable);
        } else {
            entityPage = auditLogRepository.findByTenantId(tenantId, pageable);
        }

        Page<AuditLogResponse> responsePage = entityPage.map(this::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private AuditLogResponse toResponse(AuditLogEntity entity) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .action(entity.getEventType())
                .userId(entity.getActorId() != null ? entity.getActorId().toString() : null)
                .resourceId(entity.getResourceId() != null ? entity.getResourceId().toString() : null)
                .resourceType(entity.getResourceType())
                .oldData(null) // old_data dropped in V2 jsonb migration, only changes kept
                .newData(entity.getChanges())
                .createdAt(entity.getTimestamp())
                .build();
    }
}
