package com.kemp.integration.infrastructure.messaging;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.csv.CSVRecord;
import java.util.List;

@Component
public class ChunkProcessor {
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public ChunkProcessor(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int process(List<CSVRecord> chunk) {
        java.util.UUID tenantId = com.kemp.integration.infrastructure.config.TenantContext.getTenantId();
        
        String sql = "INSERT INTO kpi_values (tenant_id, kpi_id, period_start, period_end, actual_value, progress_percent, comment, created_at, updated_at) " +
                     "VALUES (?, ?, ?::date, ?::date, ?, ?, ?, NOW(), NOW())";
                     
        List<Object[]> batchArgs = new java.util.ArrayList<>();
        for (CSVRecord record : chunk) {
            Object[] args = new Object[] {
                tenantId,
                java.util.UUID.fromString(record.get("kpi_id")),
                record.get("period_start"),
                record.get("period_end"),
                new java.math.BigDecimal(record.get("actual_value")),
                new java.math.BigDecimal(record.get("progress_percent")),
                record.isMapped("comment") ? record.get("comment") : null
            };
            batchArgs.add(args);
        }
        
        jdbcTemplate.batchUpdate(sql, batchArgs);
        
        return chunk.size();
    }
}
