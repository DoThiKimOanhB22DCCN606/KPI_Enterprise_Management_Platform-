package com.enterprise.kpi.application.dto;

import com.enterprise.kpi.domain.model.CycleType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CreateEvaluationCycleRequest {
    private String name;
    private CycleType type;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
}
