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
public class KpiProgressUpdatedEvent {
    private String eventId;
    private UUID tenantId;
    private UUID kpiId;
    private BigDecimal oldProgress;
    private BigDecimal newProgress;
    private BigDecimal target; // assuming target is included or we evaluate just the raw progress for MVP
    private long timestamp;
}
