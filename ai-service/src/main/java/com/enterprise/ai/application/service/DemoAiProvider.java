package com.enterprise.ai.application.service;

import com.enterprise.ai.application.dto.IntentExtractionResult;
import com.enterprise.ai.application.dto.QueryIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.demo-mode", havingValue = "true")
public class DemoAiProvider implements AiProvider {

    // ==========================================
    // LAYER 1: Grammar Normalizer
    // ==========================================
    private String normalizeGrammar(String input) {
        if (input == null) return "";
        String s = input.toLowerCase().trim();
        s = s.replace("suoth", "south").replace("soth", "south").replace("miền nam", "south");
        s = s.replace("norh", "north").replace("miền bắc", "north").replace("bac", "north");
        s = s.replace("hw", "why").replace("sao", "why").replace("vì sao", "why");
        s = s.replace("bad", "underperform").replace("drop", "underperform").replace("tệ", "underperform").replace("kém", "underperform");
        s = s.replace("smmary", "summary").replace("sumary", "summary").replace("tổng quan", "summary");
        s = s.replace("cmpany", "company").replace("toàn bộ", "company");
        s = s.replace("hn3", "hn003").replace("store 3", "hn003").replace("ch 3", "hn003").replace("ch hn003", "hn003");
        s = s.replace("anaylze", "analyze").replace("phân tích", "analyze").replace("xem", "analyze");
        return s;
    }

    // ==========================================
    // LAYER 2 & 3: Extraction
    // ==========================================
    private String extractEntity(String normalized) {
        if (normalized.contains("south")) return "South";
        if (normalized.contains("north")) return "North";
        if (normalized.contains("hn003")) return "STORE_003"; // Matching the DB code
        if (normalized.contains("company")) return "Company";
        return null; // No specific entity
    }

    private String extractMetric(String normalized) {
        if (normalized.contains("revenue") || normalized.contains("doanh thu")) return "Total Revenue";
        if (normalized.contains("waste") || normalized.contains("rác")) return "Waste Rate";
        if (normalized.contains("csat") || normalized.contains("hài lòng")) return "CSAT Score";
        return null;
    }

    // ==========================================
    // LAYER 4: Intent Router
    // ==========================================
    private QueryIntent routeIntent(String norm, String entity, String metric) {
        if (norm.contains("why") && norm.contains("underperform")) return QueryIntent.ROOT_CAUSE_ANALYSIS;
        if (norm.contains("why") && norm.contains("success")) return QueryIntent.ROOT_CAUSE_ANALYSIS; // or SUCCESS_ANALYSIS
        if (norm.contains("analyze") && entity != null && entity.startsWith("STORE")) return QueryIntent.STORE_ANALYSIS;
        if (norm.contains("summary") && norm.contains("company")) return QueryIntent.EXECUTIVE_SUMMARY;
        if (norm.contains("compare")) return QueryIntent.TREND_COMPARISON;
        if (norm.contains("top")) return QueryIntent.STORE_LEADERBOARD;
        if (norm.contains("worst")) return QueryIntent.BOTTOM_RANKING;
        if (norm.contains("hello") || norm.contains("hi")) return QueryIntent.GREETING;
        return QueryIntent.UNKNOWN;
    }

    @Override
    public IntentExtractionResult extractIntent(String prompt) {
        String norm = normalizeGrammar(prompt);
        String entity = extractEntity(norm);
        String metric = extractMetric(norm);
        QueryIntent intent = routeIntent(norm, entity, metric);

        log.info("NLP Pipeline - Normal: '{}', Intent: {}, Entity: {}, Metric: {}", norm, intent, entity, metric);

        IntentExtractionResult res = new IntentExtractionResult();
        res.setIntent(intent);
        if (entity != null) {
            res.setEntity1(entity);
        } else {
            res.setEntity1("%"); // fallback wildcard
        }
        res.setKpiKeyword(metric != null ? metric : "%");
        return res;
    }

    // ==========================================
    // LAYER 6: Dynamic Summary Templates
    // ==========================================
    @Override
    public String generateSummary(String prompt, List<Map<String, Object>> data) {
        String norm = normalizeGrammar(prompt);
        String entity = extractEntity(norm);
        QueryIntent intent = routeIntent(norm, entity, extractMetric(norm));

        StringBuilder sb = new StringBuilder();

        switch (intent) {
            case ROOT_CAUSE_ANALYSIS:
                sb.append(generateRootCauseTemplate(data, entity));
                break;
            case STORE_ANALYSIS:
                sb.append(generateStoreAnalysisTemplate(data, entity));
                break;
            case EXECUTIVE_SUMMARY:
                sb.append(generateExecutiveSummaryTemplate(data));
                break;
            case GREETING:
                sb.append("Hello! I am your KEMP AI Assistant. I can analyze revenue trends, compare regions, find root causes for underperformance, or summarize company data. What would you like to know?");
                break;
            default:
                sb.append("Based on the data retrieved, there are ").append(data.size()).append(" relevant records. ");
                sb.append("I am currently configured to provide deep analysis for Root Causes, Store Anomalies, and Executive Summaries. ");
                sb.append("Please try asking 'Why is South Region underperforming?' or 'Analyze Store HN003'.");
        }

        // Add Smart Follow-ups
        sb.append("\n\n**You may also ask:**\n");
        if (intent != QueryIntent.ROOT_CAUSE_ANALYSIS) sb.append("- Why is South Region underperforming in Q2?\n");
        if (intent != QueryIntent.EXECUTIVE_SUMMARY) sb.append("- Summarize company performance\n");
        if (intent != QueryIntent.STORE_ANALYSIS) sb.append("- Analyze Store HN003\n");

        return sb.toString();
    }

    private String generateRootCauseTemplate(List<Map<String, Object>> data, String entity) {
        if (data == null || data.isEmpty()) return "No significant data found for " + entity + " to determine root cause.";
        
        // Find metrics in the returned SQL data
        double revenue = extractMetricAvg(data, "Total Revenue");
        double waste = extractMetricAvg(data, "Waste Rate");
        double csat = extractMetricAvg(data, "CSAT Score");
        
        // Convert to billion VND for display
        double revBillion = revenue / 1_000_000_000.0;

        return String.format(
            "Based on recent analytics, %s generated %.1fB VND in revenue. " +
            "This correlates strongly with a %.1f%% spike in Waste Rate and a drop in CSAT to %.1f%%. " +
            "These indicators suggest severe operational inefficiencies rather than weak customer demand.",
            entity != null ? entity : "the region", revBillion, waste, csat
        );
    }

    private String generateStoreAnalysisTemplate(List<Map<String, Object>> data, String entity) {
        if (data == null || data.isEmpty()) return "No anomalies found for " + entity + ".";
        
        double csat = extractMetricAvg(data, "CSAT Score");
        double oos = extractMetricAvg(data, "Out of Stock Rate");

        return String.format(
            "Store %s presents an unusual profile: Customer Satisfaction (CSAT) is excellent at %.1f%% (Top 10%%). " +
            "However, logistical execution is poor: Out-of-Stock rates sit at %.1f%%. " +
            "This suggests the front-line staff are extremely friendly, but the back-end warehouse team is failing to keep shelves stocked.",
            entity != null ? entity : "HN003", csat, oos
        );
    }

    private String generateExecutiveSummaryTemplate(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "Company performance data is currently unavailable.";
        
        double totalRev = extractMetricAvg(data, "Total Revenue");
        double totalRevB = totalRev / 1_000_000_000.0;
        
        return String.format(
            "Overall company performance is stable, having tracked %.1fB VND in regional revenue rollups. " +
            "However, there is a stark regional contrast: The North is highly efficient, the South is facing turnover-driven revenue drops, " +
            "and the Central region holds the highest profit margins despite lower volume.",
            totalRevB * 3 // Roughly extrapolating company total from regional averages for mock
        );
    }

    private double extractMetricAvg(List<Map<String, Object>> data, String metricName) {
        for (Map<String, Object> row : data) {
            String kpiName = (String) row.get("kpi_name");
            if (kpiName != null && kpiName.contains(metricName)) {
                Number val = (Number) row.get("actual_value");
                if (val != null) return val.doubleValue();
            }
        }
        return 0.0;
    }
}
