package com.enterprise.analytics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataDTO {
    private String dimension; // e.g., "REGION", "STORE"
    private String dimensionId;
    private List<DataPoint> dataPoints;
    private BigDecimal variance;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataPoint {
        private String period; // e.g., "2026-Q1"
        private BigDecimal value;
    }
}
