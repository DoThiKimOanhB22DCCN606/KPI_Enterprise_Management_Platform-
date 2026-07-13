package com.enterprise.ai.application.service.texttosql;

import com.enterprise.ai.infrastructure.client.AnalyticsServiceClient;
import com.enterprise.ai.infrastructure.config.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Demo Service for Mentor Presentation - Main Execution Flow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiQueryExecutionService {

    private final TextToSqlGenerator textToSqlGenerator;
    private final QuerySecurityValidator securityValidator;
    private final AnalyticsServiceClient analyticsClient;

    public List<Map<String, Object>> processNaturalLanguageQuery(String userQuestion) {
        UUID tenantId = TenantContext.getTenantId();
        log.info("Processing NL Query for Tenant {}: {}", tenantId, userQuestion);

        // 1. Text-to-SQL via Spring AI & Groq/OpenAI
        String rawSql = textToSqlGenerator.generateSqlFromPrompt(userQuestion);

        // 2. Layer 3 Security Check (JSqlParser + AST Manipulation)
        String securedSql = securityValidator.validateAndSecureQuery(rawSql, tenantId.toString());

        // 3. Execution against PostgreSQL
        log.info("Dispatching secured query to Analytics Service...");
        
        // Execute the query safely
        return analyticsClient.executeQuery(securedSql, tenantId, new HashMap<>());
    }
}
