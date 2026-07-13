package com.enterprise.ai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private List<RecommendedKpi> recommendedKpis;
    private String summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedKpi {
        private String name;
        private String description;
        private String suggestedTarget;
        private String rationale;
    }
}
