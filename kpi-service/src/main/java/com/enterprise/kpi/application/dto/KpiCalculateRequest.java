package com.enterprise.kpi.application.dto;

import lombok.Data;

import java.util.Map;

@Data
public class KpiCalculateRequest {
    private Map<String, Double> variables;
}
