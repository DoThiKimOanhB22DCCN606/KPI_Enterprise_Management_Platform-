package com.enterprise.goal.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Data
public class GoalEntity {
    @Id
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
