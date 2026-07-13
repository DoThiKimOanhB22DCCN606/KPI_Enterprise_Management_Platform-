package com.enterprise.ai.application.service;

import com.enterprise.ai.application.dto.QueryIntent;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class CannedQueryRegistry {

    private final Map<QueryIntent, String> templates = new EnumMap<>(QueryIntent.class);

    public CannedQueryRegistry() {
        templates.put(QueryIntent.STORE_LEADERBOARD,
            "SELECT store_name, total_revenue, kpi_score " +
            "FROM vw_store_leaderboard " +
            "WHERE tenant_id = :tenantId AND (store_name LIKE :keyword ESCAPE '\\') " +
            "ORDER BY kpi_score %s " +
            "LIMIT :limit"
        );

        templates.put(QueryIntent.KPI_SUMMARY,
            "SELECT status, count(*) as count, avg(target) as avg_target " +
            "FROM vw_kpi_summary " +
            "WHERE tenant_id = :tenantId AND (name LIKE :keyword ESCAPE '\\') " +
            "GROUP BY status " +
            "ORDER BY count %s " +
            "LIMIT :limit"
        );

        templates.put(QueryIntent.EMPLOYEE_PERFORMANCE,
            "SELECT employee_name, department, score " +
            "FROM vw_employee_performance " +
            "WHERE tenant_id = :tenantId AND (employee_name LIKE :keyword ESCAPE '\\') " +
            "ORDER BY score %s " +
            "LIMIT :limit"
        );

        templates.put(QueryIntent.TREND_COMPARISON,
            "SELECT kv.period_start, store.name AS store_name, k.name AS kpi_name, AVG(kv.actual_value) AS actual_value " +
            "FROM kpi_values kv " +
            "JOIN kpis k ON kv.kpi_id = k.id " +
            "JOIN users u ON k.owner_id = u.id AND k.owner_type = 'USER' " +
            "JOIN organization_units sub_ou ON u.organization_unit_id = sub_ou.id " +
            "JOIN organization_units store ON sub_ou.path <@ store.path AND store.type = 'STORE' " +
            "WHERE kv.tenant_id = :tenantId " +
            "  AND (k.name ILIKE :kpiKeyword) " +
            "  AND (store.name ILIKE :entity1 OR store.name ILIKE :entity2) " +
            "  AND kv.period_start >= CURRENT_DATE - CAST(:months || ' months' AS INTERVAL) " +
            "GROUP BY store.name, kv.period_start, k.name " +
            "ORDER BY kv.period_start %s"
        );
        
        templates.put(QueryIntent.ROOT_CAUSE_ANALYSIS,
            "SELECT k.name AS kpi_name, AVG(kv.actual_value) AS actual_value, AVG(k.target_value) AS target_value " +
            "FROM kpi_values kv " +
            "JOIN kpis k ON kv.kpi_id = k.id " +
            "JOIN users u ON k.owner_id = u.id AND k.owner_type = 'USER' " +
            "JOIN organization_units ou ON u.organization_unit_id = ou.id " +
            "WHERE kv.tenant_id = :tenantId " +
            "  AND (ou.name ILIKE :entity1 OR ou.code ILIKE :entity1) " +
            "GROUP BY k.name " +
            "ORDER BY k.name %s"
        );
        
        templates.put(QueryIntent.STORE_ANALYSIS,
            "SELECT k.name AS kpi_name, AVG(kv.actual_value) AS actual_value " +
            "FROM kpi_values kv " +
            "JOIN kpis k ON kv.kpi_id = k.id " +
            "JOIN users u ON k.owner_id = u.id AND k.owner_type = 'USER' " +
            "JOIN organization_units ou ON u.organization_unit_id = ou.id " +
            "WHERE kv.tenant_id = :tenantId " +
            "  AND (ou.name ILIKE :entity1 OR ou.code ILIKE :entity1) " +
            "GROUP BY k.name " +
            "ORDER BY k.name %s"
        );
        
        templates.put(QueryIntent.EXECUTIVE_SUMMARY,
            "SELECT k.name AS kpi_name, AVG(kv.actual_value) AS actual_value " +
            "FROM kpi_values kv " +
            "JOIN kpis k ON kv.kpi_id = k.id " +
            "JOIN users u ON k.owner_id = u.id AND k.owner_type = 'USER' " +
            "JOIN organization_units ou ON u.organization_unit_id = ou.id " +
            "WHERE kv.tenant_id = :tenantId " +
            "  AND ou.level = 1 " + // Regions
            "GROUP BY k.name " +
            "ORDER BY actual_value %s"
        );
    }

    public String getTemplate(QueryIntent intent, String sortOrder) {
        String template = templates.get(intent);
        if (template == null) {
            // Fallback to a dummy query so pipeline doesn't crash on mocked intents
            return "SELECT 1 as dummy WHERE :tenantId = :tenantId";
        }
        
        String sortLiteral = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        
        return String.format(template, sortLiteral);
    }
}
