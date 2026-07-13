package com.enterprise.ai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlQueryResponse {
    private String generatedSql;
    private String explanation;
    private String humanResponse;
    private List<Map<String, Object>> rawData;
    private UUID conversationId;
}
