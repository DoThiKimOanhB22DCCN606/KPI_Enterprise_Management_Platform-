package com.enterprise.ai.application.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RecommendationRequest {
    private String tenantId;
    private String departmentType;
    private Map<String, Object> historicalMetrics; // e.g., {"revenue_growth": "-5%", "attrition": "12%"}
}
