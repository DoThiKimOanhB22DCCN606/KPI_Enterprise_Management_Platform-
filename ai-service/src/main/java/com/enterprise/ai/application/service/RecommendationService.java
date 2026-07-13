package com.enterprise.ai.application.service;

import com.enterprise.ai.application.dto.RecommendationRequest;
import com.enterprise.ai.application.dto.RecommendationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecommendationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public RecommendationService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        log.info("Generating KPI recommendations for tenant: {}", request.getTenantId());

        String historicalDataJson;
        try {
            historicalDataJson = objectMapper.writeValueAsString(request.getHistoricalMetrics());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse historical metrics", e);
        }

        // Prompt injection prevention for user input
        String sanitizedDepartment = request.getDepartmentType() != null 
                ? request.getDepartmentType().replaceAll("(?i)(ignore|bypass|forget)", "").trim() 
                : "General";

        // Configure Spring AI to parse the LLM output directly into our Java DTO
        var outputConverter = new BeanOutputConverter<>(RecommendationResponse.class);

        String systemPrompt = """
                You are a Senior Data Analyst for KEMP.
                Suggest 3 new KPIs they should focus on for the next quarter.
                """;

        String userTemplate = """
                Based on the following historical performance data for a '{department}' department:
                {historicalData}
                
                {format}
                """;

        try {
            String responseStr = chatClient.prompt()
                    .system(systemPrompt)
                    .user(u -> u.text(userTemplate)
                            .param("department", sanitizedDepartment)
                            .param("historicalData", historicalDataJson)
                            .param("format", outputConverter.getFormat()))
                    .call()
                    .content();

            return outputConverter.convert(responseStr);

        } catch (Exception e) {
            log.error("Failed to generate recommendations", e);
            throw new RuntimeException("Failed to generate recommendations via LLM", e);
        }
    }
}
