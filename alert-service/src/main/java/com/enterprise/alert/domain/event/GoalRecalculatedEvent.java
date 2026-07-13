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
public class GoalRecalculatedEvent {
    private String eventId;
    private UUID tenantId;
    private UUID goalId;
    private BigDecimal overallProgress;
    private long timestamp;
}
