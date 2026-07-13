package com.enterprise.analytics.interfaces.rest;

import com.enterprise.analytics.application.dto.ExecuteQueryRequest;
import com.enterprise.analytics.application.dto.LeaderboardEntryDTO;
import com.enterprise.analytics.application.dto.TrendDataPoint;
import com.enterprise.analytics.application.dto.VarianceResult;
import com.enterprise.analytics.application.service.LeaderboardService;
import com.enterprise.analytics.application.service.TrendAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final LeaderboardService leaderboardService;
    private final TrendAggregationService trendService;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private UUID getMockTenantId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @GetMapping("/kpis/{kpiId}/trend")
    public ResponseEntity<List<TrendDataPoint>> getTrend(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdHeader,
            @PathVariable UUID kpiId) {
        UUID tenantId = tenantIdHeader != null ? UUID.fromString(tenantIdHeader) : getMockTenantId();
        return ResponseEntity.ok(trendService.getKpiTrend(kpiId, tenantId));
    }

    @GetMapping("/kpis/{kpiId}/variance")
    public ResponseEntity<VarianceResult> getVariance(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdHeader,
            @PathVariable UUID kpiId,
            @RequestParam LocalDate currentPeriod,
            @RequestParam LocalDate previousPeriod) {
        UUID tenantId = tenantIdHeader != null ? UUID.fromString(tenantIdHeader) : getMockTenantId();
        return ResponseEntity.ok(trendService.getVariance(kpiId, tenantId, currentPeriod, previousPeriod));
    }

    @GetMapping("/leaderboard/{type}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard(
            @PathVariable String type,
            @RequestParam String period,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getTopN(type, period, limit));
    }

    @PostMapping("/leaderboard/{type}/score")
    public ResponseEntity<Void> updateScore(
            @PathVariable String type,
            @RequestParam String period,
            @RequestParam String entityId,
            @RequestParam double score) {
        leaderboardService.updateScore(type, period, entityId, score);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leaderboard/{type}/reset")
    public ResponseEntity<Void> resetLeaderboard(
            @PathVariable String type,
            @RequestParam String period) {
        leaderboardService.reset(type, period);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/kpis/{kpiId}/performance")
    public ResponseEntity<Map<String, Object>> getPerformance(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdHeader,
            @PathVariable UUID kpiId) {
        UUID tenantId = tenantIdHeader != null ? UUID.fromString(tenantIdHeader) : getMockTenantId();
        return ResponseEntity.ok(trendService.getKpiPerformance(kpiId, tenantId));
    }

    @PostMapping("/execute")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(@RequestBody ExecuteQueryRequest request) {
        System.out.println("RECEIVED ExecuteQueryRequest: " + request);
        String sql = request.getSql();
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) {
                throw new IllegalArgumentException("Query must be a SELECT statement");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SQL query or syntax error: " + e.getMessage());
        }

        if (!sql.contains("tenant_id = ?") && !sql.contains("tenant_id = :tenantId")) {
            throw new IllegalArgumentException("Query must contain tenant_id filter");
        }

        if (sql.trim().endsWith(";")) {
            sql = sql.trim();
            sql = sql.substring(0, sql.length() - 1);
        }
        if (!sql.toUpperCase().contains("LIMIT")) {
            sql = sql + " LIMIT 1000";
        }

        List<Map<String, Object>> result;
        java.util.Map<String, Object> params = request.getParameters() != null ? new java.util.HashMap<>(request.getParameters()) : new java.util.HashMap<>();
        params.put("tenantId", UUID.fromString(request.getTenantId()));
        result = namedParameterJdbcTemplate.queryForList(sql, params);
        return ResponseEntity.ok(result);
    }
}
