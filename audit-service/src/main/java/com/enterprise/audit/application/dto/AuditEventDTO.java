package com.enterprise.audit.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDTO {
    private String eventId;
    private String eventType; // e.g. "USER_LOGIN", "KPI_UPDATED", "PERMISSION_GRANTED"
    private String actorId;   // The user who performed the action
    private String resourceId; // The ID of the resource affected
    private JsonNode changes; // JSON object with 'before' and 'after' keys
    private long timestamp;
}
