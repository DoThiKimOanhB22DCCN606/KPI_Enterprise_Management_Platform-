package com.enterprise.audit.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for audit log entries returned by the REST API.
 * Maps from AuditLogEntity using the existing field names in the entity.
 *
 * Note: AuditLogEntity does not store tenantId — actorId maps to userId
 * and eventType maps to action for API compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;

    /** Maps to AuditLogEntity.eventType */
    private String action;

    /** Maps to AuditLogEntity.actorId */
    private String userId;

    /** Maps to AuditLogEntity.resourceId */
    private String resourceId;

    /** Derived from resourceId prefix (e.g. "KPI", "USER") if encoded, else null */
    private String resourceType;

    /** Maps to AuditLogEntity.oldVal */
    private JsonNode oldData;

    /** Maps to AuditLogEntity.newVal */
    private JsonNode newData;

    /** Maps to AuditLogEntity.timestamp */
    private OffsetDateTime createdAt;
}
