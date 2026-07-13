package com.enterprise.kpi.application.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class KpiTemplateUpdateRequest {
    private String name;
    private String description;
    private String category;
    private String defaultFrequency;
    private BigDecimal defaultTarget;
    private String defaultFormula;
}
