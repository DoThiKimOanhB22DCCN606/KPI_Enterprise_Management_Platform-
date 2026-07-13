package com.enterprise.alert.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTriggeredEvent {
    private String eventId;
    private UUID tenantId;
    private String sourceId;
    private String sourceType; // "KPI" or "GOAL"
    private Severity severity;
    private String message;
    private long timestamp;
}
