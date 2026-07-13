package com.enterprise.ai.application.service.texttosql;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Component;

/**
 * Demo Service for Mentor Presentation - Security Validation Layer
 */
@Slf4j
@Component
public class QuerySecurityValidator {

    public String validateAndSecureQuery(String rawSql, String tenantId) {
        log.info("Validating SQL query for security risks...");

        try {
            // 1. Parse the SQL using JSqlParser
            Statement statement = CCJSqlParserUtil.parse(rawSql);

            // 2. Strict Domain Guard Clause: Only SELECT is allowed
            if (!(statement instanceof Select)) {
                log.error("Security violation: Attempted to execute non-SELECT query.");
                throw new SecurityException("Phát hiện truy vấn nguy hiểm. Hệ thống chỉ cho phép thực thi lệnh SELECT.");
            }

            // 3. Multi-tenant Isolation Enforcement
            // In a real scenario, JSqlParser would traverse the AST and inject WHERE tenant_id = '...'
            // For the demo presentation, we show the concept of AST manipulation intercept:
            String securedSql = enforceTenantIsolation((Select) statement, tenantId);

            log.info("SQL Validation Passed. Secured Query: {}", securedSql);
            return securedSql;

        } catch (Exception e) {
            log.error("SQL Parsing failed or malicious payload detected: {}", e.getMessage());
            throw new SecurityException("Câu lệnh truy vấn không hợp lệ hoặc vi phạm chính sách bảo mật.", e);
        }
    }

    private String enforceTenantIsolation(Select selectStatement, String tenantId) {
        // Concept demonstration of AST manipulation to force tenant_id
        // (Note: The actual project relies on RLS Interceptor at the JDBC level, 
        // but this shows the application-layer security approach mentioned in the presentation).
        String originalSql = selectStatement.toString();
        
        if (!originalSql.toLowerCase().contains("tenant_id")) {
            log.warn("Query lacked tenant isolation. Injecting strict tenant filter...");
            // Simulated AST injection for presentation
            return "SELECT * FROM (" + originalSql + ") AS secured_subquery WHERE tenant_id = '" + tenantId + "'";
        }
        
        return originalSql;
    }
}
