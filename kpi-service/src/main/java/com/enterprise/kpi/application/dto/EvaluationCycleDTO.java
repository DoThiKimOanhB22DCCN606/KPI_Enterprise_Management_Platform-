package com.enterprise.kpi.application.dto;

import com.enterprise.kpi.domain.model.CycleStatus;
import com.enterprise.kpi.domain.model.CycleType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class EvaluationCycleDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private CycleType type;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private CycleStatus status;
    private int kpiCount;
}
