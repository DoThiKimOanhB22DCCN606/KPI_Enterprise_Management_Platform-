package com.enterprise.kpi.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDTO {
    private String eventId;
    private String eventType;
    private String actorId;
    private String resourceId;
    private Map<String, Object> changes; // Serialize as JSON object
    private long timestamp;
}
