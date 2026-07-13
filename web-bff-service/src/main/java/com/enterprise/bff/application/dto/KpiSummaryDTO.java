package com.enterprise.bff.application.dto;

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
public class KpiSummaryDTO {
    private UUID id;
    private String templateName;
    private String status;
    private BigDecimal target;
    private BigDecimal currentProgress;
}
