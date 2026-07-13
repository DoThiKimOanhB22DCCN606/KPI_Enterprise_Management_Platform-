package com.enterprise.goal.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalRecalculatedEvent {
    private UUID goalId;
    private UUID tenantId;
    private double newProgress;
}
