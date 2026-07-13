package com.enterprise.ai.application.service.texttosql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Demo Service for Mentor Presentation - Text to SQL Generation
 */
@Slf4j
@Service
public class TextToSqlGenerator {

    private final ChatClient chatClient;

    private static final String DATABASE_SCHEMA = """
        Table: kpis (id UUID, name VARCHAR, target_value NUMERIC, owner_id UUID, cycle_id UUID)
        Table: kpi_values (id UUID, kpi_id UUID, actual_value NUMERIC, period_start DATE, tenant_id UUID)
        Table: users (id UUID, full_name VARCHAR, organization_unit_id UUID)
        """;

    private static final String SYSTEM_PROMPT = """
        You are a highly advanced PostgreSQL expert. 
        Your task is to convert the user's natural language question into a valid PostgreSQL SELECT statement.
        Use ONLY the following database schema:
        %s
        
        CRITICAL RULES:
        1. Return ONLY the raw SQL query. Do not wrap in markdown (```sql).
        2. NEVER generate UPDATE, DELETE, INSERT, or DROP statements.
        3. Assume multi-tenant architecture.
        """;

    public TextToSqlGenerator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(String.format(SYSTEM_PROMPT, DATABASE_SCHEMA))
                .build();
    }

    public String generateSqlFromPrompt(String userPrompt) {
        log.info("Generating SQL for prompt: {}", userPrompt);
        
        String generatedSql = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();
                
        log.info("AI Generated SQL: {}", generatedSql);
        return generatedSql.trim();
    }
}
