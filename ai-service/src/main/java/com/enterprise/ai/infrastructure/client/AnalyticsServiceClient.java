package com.enterprise.ai.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AnalyticsServiceClient {
    private final WebClient webClient;
    
    public AnalyticsServiceClient(@Value("${analytics.service.url:http://analytics-service:8088}") String analyticsUrl) {
        this.webClient = WebClient.builder().baseUrl(analyticsUrl).build();
    }
    
    public List<Map<String,Object>> executeQuery(String sql, UUID tenantId, Map<String, Object> parameters) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authHeader = attributes != null ? attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("sql", sql);
        body.put("tenantId", tenantId.toString());
        body.put("parameters", parameters);

        return webClient.post()
            .uri("/v1/analytics/execute")
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<Map<String,Object>>() {})
            .collectList()
            .timeout(Duration.ofSeconds(30))
            .block();
    }
}
