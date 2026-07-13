package com.enterprise.ai.application.service;

import com.enterprise.ai.application.dto.IntentExtractionResult;
import com.enterprise.ai.application.dto.NlQueryRequest;
import com.enterprise.ai.application.dto.NlQueryResponse;
import com.enterprise.ai.application.dto.QueryIntent;
import com.enterprise.ai.infrastructure.client.AnalyticsServiceClient;
import com.enterprise.ai.infrastructure.persistence.AiConversationEntity;
import com.enterprise.ai.infrastructure.persistence.AiConversationRepository;
import com.enterprise.ai.infrastructure.persistence.AiMessageEntity;
import com.enterprise.ai.infrastructure.persistence.AiMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class NlpAnalyticsService {

    private final AiProvider aiProvider;
    private final AnalyticsServiceClient analyticsClient;
    private final AiConversationRepository conversationRepo;
    private final AiMessageRepository messageRepo;
    private final CannedQueryRegistry cannedQueryRegistry;

    public NlpAnalyticsService(AiProvider aiProvider, 
                            AnalyticsServiceClient analyticsClient,
                            AiConversationRepository conversationRepo,
                            AiMessageRepository messageRepo,
                            CannedQueryRegistry cannedQueryRegistry) {
        this.aiProvider = aiProvider;
        this.analyticsClient = analyticsClient;
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.cannedQueryRegistry = cannedQueryRegistry;
    }

    public NlQueryResponse translate(NlQueryRequest request) {
        log.info("Processing NL query: {}", request.getPrompt());

        UUID tenantId = com.enterprise.ai.infrastructure.config.TenantContext.getTenantId();
        UUID userId = (UUID) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID conversationId = request.getConversationId();
        if (conversationId == null) {
            conversationId = UUID.randomUUID();
            AiConversationEntity conv = new AiConversationEntity();
            conv.setId(conversationId);
            conv.setUserId(userId);
            conv.setTenantId(tenantId);
            conv.setCreatedAt(Instant.now());
            conv.setUpdatedAt(Instant.now());
            conversationRepo.save(conv);
        }

        // 1. Intent Extraction
        IntentExtractionResult intentResult = null;
        try {
            intentResult = aiProvider.extractIntent(request.getPrompt());
        } catch (Exception e) {
            log.error("Failed to parse intent from LLM", e);
            String errorMsg = e.getMessage() != null && e.getMessage().contains("429") 
                ? "Hệ thống AI đang bị quá tải do giới hạn API miễn phí. Vui lòng đợi 15 giây và thử lại." 
                : "Xin lỗi, đã xảy ra lỗi khi kết nối với AI để phân tích yêu cầu.";
            saveMessages(conversationId, request.getPrompt(), errorMsg, "ERROR");
            return NlQueryResponse.builder()
                    .humanResponse(errorMsg)
                    .conversationId(conversationId)
                    .build();
        }

        // 2. Fallback
        if (intentResult == null || intentResult.getIntent() == null || intentResult.getIntent() == QueryIntent.UNKNOWN) {
            String fallback = "Xin lỗi, tôi chưa được huấn luyện để phân tích loại dữ liệu này.";
            saveMessages(conversationId, request.getPrompt(), fallback, "UNKNOWN_INTENT");
            return NlQueryResponse.builder()
                    .humanResponse(fallback)
                    .conversationId(conversationId)
                    .build();
        }

        // 3. Execution Preparation
        String sqlTemplate = cannedQueryRegistry.getTemplate(intentResult.getIntent(), intentResult.getSortOrder());
        
        // Parameter Clamping
        int safeLimit = 10;
        if (intentResult.getLimit() != null) {
            safeLimit = Math.max(1, Math.min(intentResult.getLimit(), 100));
        }

        // Search Keyword Wildcard Escaping
        String rawKeyword = intentResult.getSearchKeyword();
        String safeKeyword = "%";
        if (rawKeyword != null && !rawKeyword.trim().isEmpty()) {
            String escaped = rawKeyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
            safeKeyword = "%" + escaped + "%";
        }

        Map<String, Object> params = new HashMap<>();
        params.put("limit", safeLimit);
        params.put("keyword", safeKeyword);
        
        params.put("entity1", "%" + (intentResult.getEntity1() != null ? intentResult.getEntity1() : "") + "%");
        params.put("entity2", "%" + (intentResult.getEntity2() != null ? intentResult.getEntity2() : "") + "%");
        params.put("kpiKeyword", "%" + (intentResult.getKpiKeyword() != null ? intentResult.getKpiKeyword() : "") + "%");
        params.put("months", intentResult.getMonths() != null ? intentResult.getMonths() : 3);

        // 4. Execution
        List<Map<String, Object>> data;
        try {
            log.info("Executing SQL: {}", sqlTemplate);
            log.info("With params: {}", params);
            data = analyticsClient.executeQuery(sqlTemplate, tenantId, params);
        } catch (Exception e) {
            log.error("Execution failed", e);
            String fallback = "Có lỗi xảy ra khi truy vấn dữ liệu.";
            saveMessages(conversationId, request.getPrompt(), fallback, sqlTemplate);
            throw new RuntimeException("Query execution failed", e);
        }

        // 5. Data Interpretation
        String humanResponse = "";
        if (!data.isEmpty()) {
            try {
                humanResponse = aiProvider.generateSummary(request.getPrompt(), data);
            } catch (Exception e) {
                log.error("Failed to generate human response from LLM", e);
                humanResponse = "Dữ liệu đã được lấy thành công nhưng không thể tổng hợp thành câu do lỗi kết nối AI.";
            }
        } else {
            humanResponse = "Không tìm thấy dữ liệu phù hợp với yêu cầu của bạn.";
        }

        saveMessages(conversationId, request.getPrompt(), humanResponse, sqlTemplate);

        return NlQueryResponse.builder()
                .generatedSql(sqlTemplate)
                .humanResponse(humanResponse)
                .rawData(data)
                .conversationId(conversationId)
                .build();
    }

    private void saveMessages(UUID conversationId, String userPrompt, String aiResponse, String sql) {
        AiMessageEntity userMsg = new AiMessageEntity();
        userMsg.setId(UUID.randomUUID());
        userMsg.setConversationId(conversationId);
        userMsg.setRole("USER");
        userMsg.setContent(userPrompt);
        userMsg.setCreatedAt(Instant.now());
        messageRepo.save(userMsg);

        AiMessageEntity aiMsg = new AiMessageEntity();
        aiMsg.setId(UUID.randomUUID());
        aiMsg.setConversationId(conversationId);
        aiMsg.setRole("AI");
        aiMsg.setContent(aiResponse);
        aiMsg.setSqlQuery(sql);
        aiMsg.setCreatedAt(Instant.now());
        messageRepo.save(aiMsg);
    }
}
