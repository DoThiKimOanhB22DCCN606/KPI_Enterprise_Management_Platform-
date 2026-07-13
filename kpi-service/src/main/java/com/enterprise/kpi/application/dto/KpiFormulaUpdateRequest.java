package com.enterprise.kpi.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KpiFormulaUpdateRequest {
    @NotBlank(message = "formula is required")
    private String formula;
}
