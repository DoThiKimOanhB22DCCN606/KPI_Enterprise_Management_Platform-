package com.enterprise.goal.domain.model;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class Goal {
    private UUID id;
    private UUID tenantId;
    private UUID parentGoalId;
    private UUID kpiId;
    private String name;
    private String description;
    private double targetValue;
    private double currentValue;
    private double overallProgress;
    private double weight;
    private UUID ownerId;
    private String ownerType;
    private String status;
}
