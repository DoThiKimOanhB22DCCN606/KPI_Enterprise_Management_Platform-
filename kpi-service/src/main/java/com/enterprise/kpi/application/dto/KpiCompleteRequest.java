package com.enterprise.kpi.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class KpiCompleteRequest {
    @NotNull(message = "Evaluation score is required")
    private BigDecimal finalScore;
    private String managerComments;
}
