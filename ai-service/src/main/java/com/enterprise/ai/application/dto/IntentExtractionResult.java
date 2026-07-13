package com.enterprise.ai.application.dto;

import lombok.Data;

@Data
public class IntentExtractionResult {
    private QueryIntent intent;
    private Integer limit;
    private String sortOrder;
    private String searchKeyword;
    
    // For TREND_COMPARISON
    private String entity1;
    private String entity2;
    private String kpiKeyword;
    private Integer months;
}
