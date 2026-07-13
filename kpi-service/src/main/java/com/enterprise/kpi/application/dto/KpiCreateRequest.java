package com.enterprise.kpi.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class KpiCreateRequest {
    
    @NotNull(message = "templateId is required")
    private UUID templateId;
    
    @NotNull(message = "ownerId is required")
    private UUID ownerId;
    
    @NotBlank(message = "frequency is required")
    private String frequency;
    
    @NotNull(message = "target is required")
    @DecimalMin(value = "0.0", message = "target must be at least 0.0")
    private BigDecimal target;
    
    private String formula;
    
    private UUID cycleId;
}
