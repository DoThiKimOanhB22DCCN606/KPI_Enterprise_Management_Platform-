package com.enterprise.kpi.application.dto;

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
public class KpiTemplateDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private String category;
    private String defaultFrequency;
    private BigDecimal defaultTarget;
    private String defaultFormula;
}
