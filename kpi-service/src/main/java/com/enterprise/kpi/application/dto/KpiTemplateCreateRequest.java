package com.enterprise.kpi.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class KpiTemplateCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private String category;
    private String defaultFrequency;
    private BigDecimal defaultTarget;
    private String defaultFormula;
}
