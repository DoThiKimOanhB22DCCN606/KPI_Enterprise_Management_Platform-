package com.enterprise.analytics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VarianceResult {
    private Double currentValue;
    private Double previousValue;
    private Double variancePct;
}
