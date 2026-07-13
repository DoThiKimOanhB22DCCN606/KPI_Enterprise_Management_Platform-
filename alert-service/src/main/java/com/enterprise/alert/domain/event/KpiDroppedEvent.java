package com.enterprise.alert.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDroppedEvent {
    private String eventId;
    private UUID tenantId;
    private UUID kpiId;
    private BigDecimal currentProgress;
    private Severity severity;
    private String message;
    private long timestamp;
}
