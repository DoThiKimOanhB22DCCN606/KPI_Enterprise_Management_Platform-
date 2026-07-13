package com.enterprise.kpi.application.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UpdateEvaluationCycleRequest {
    private String name;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private String reason;
}
