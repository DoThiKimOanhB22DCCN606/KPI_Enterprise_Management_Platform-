package com.enterprise.analytics.infrastructure.persistence;

import com.enterprise.analytics.application.dto.TrendDataPoint;
import com.enterprise.analytics.application.dto.VarianceResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public class TrendAnalysisRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<TrendDataPoint> getKpiTrend(UUID kpiId, UUID tenantId) {
        String sql = """
            SELECT DATE_TRUNC('month', period_start) as month,
                   AVG(actual_value) as avg_value,
                   MAX(actual_value) as max_value,
                   MIN(actual_value) as min_value,
                   COUNT(*) as data_points
            FROM kpi_values
            WHERE kpi_id = :kpiId AND tenant_id = :tenantId
              AND period_start >= NOW() - INTERVAL '12 months'
            GROUP BY DATE_TRUNC('month', period_start)
            ORDER BY month ASC
            """;
            
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("kpiId", kpiId);
        query.setParameter("tenantId", tenantId);
        
        List<Object[]> rows = query.getResultList();
        List<TrendDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new TrendDataPoint(
                (Date) row[0],
                ((Number) row[1]).doubleValue(),
                ((Number) row[2]).doubleValue(),
                ((Number) row[3]).doubleValue(),
                ((Number) row[4]).longValue()
            ));
        }
        return result;
    }

    public VarianceResult getVariance(UUID kpiId, UUID tenantId, 
                                      LocalDate currentPeriodStart, LocalDate previousPeriodStart) {
        String sql = """
            SELECT 
                curr.actual_value as current_value,
                prev.actual_value as previous_value,
                CASE WHEN prev.actual_value != 0 
                     THEN ((curr.actual_value - prev.actual_value) / ABS(prev.actual_value)) * 100 
                     ELSE 0 END as variance_pct
            FROM kpi_values curr
            LEFT JOIN kpi_values prev ON prev.kpi_id = curr.kpi_id 
                AND DATE_TRUNC('month', prev.period_start) = :previousPeriodStart
                AND prev.tenant_id = :tenantId
            WHERE curr.kpi_id = :kpiId 
              AND DATE_TRUNC('month', curr.period_start) = :currentPeriodStart
              AND curr.tenant_id = :tenantId
            LIMIT 1
            """;
            
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("kpiId", kpiId);
        query.setParameter("tenantId", tenantId);
        query.setParameter("currentPeriodStart", currentPeriodStart);
        query.setParameter("previousPeriodStart", previousPeriodStart);
        
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return new VarianceResult(0.0, 0.0, 0.0);
        }
        Object[] row = rows.get(0);
        
        Double currentVal = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
        Double previousVal = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
        Double variancePct = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
        
        return new VarianceResult(currentVal, previousVal, variancePct);
    }

    public java.util.Map<String, Object> getKpiPerformance(UUID kpiId, UUID tenantId) {
        String sql = """
            SELECT 
                AVG(actual_value) as avg_value,
                MAX(target_value) as max_target
            FROM kpi_values
            WHERE kpi_id = :kpiId AND tenant_id = :tenantId
            """;
            
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("kpiId", kpiId);
        query.setParameter("tenantId", tenantId);
        
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty() || rows.get(0)[0] == null) {
            return java.util.Map.of(
                "kpiId", kpiId,
                "currentValue", 0.0,
                "targetValue", 0.0,
                "performancePct", 0.0
            );
        }
        
        Object[] row = rows.get(0);
        Double currentVal = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
        Double targetVal = row[1] != null ? ((Number) row[1]).doubleValue() : 100.0;
        Double performancePct = targetVal != 0 ? (currentVal / targetVal) * 100 : 0.0;
        
        return java.util.Map.of(
            "kpiId", kpiId,
            "currentValue", currentVal,
            "targetValue", targetVal,
            "performancePct", performancePct
        );
    }
}
