package com.enterprise.kpi.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class KpiProgressRecordRequest {

    @NotNull(message = "value is required")
    private BigDecimal value;

    @NotNull(message = "periodStart is required")
    private OffsetDateTime periodStart;

    @NotNull(message = "periodEnd is required")
    private OffsetDateTime periodEnd;

    private UUID valueId;

    private String notes;

    private Map<String, Object> evidence;
}
