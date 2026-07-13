package com.enterprise.ai.interfaces.rest;

import com.enterprise.ai.application.dto.NlQueryRequest;
import com.enterprise.ai.application.dto.NlQueryResponse;
import com.enterprise.ai.application.dto.RecommendationRequest;
import com.enterprise.ai.application.dto.RecommendationResponse;
import com.enterprise.ai.application.service.RecommendationService;
import com.enterprise.ai.application.service.NlpAnalyticsService;
import com.enterprise.ai.infrastructure.persistence.AiConversationEntity;
import com.enterprise.ai.infrastructure.persistence.AiConversationRepository;
import com.enterprise.ai.infrastructure.persistence.AiMessageEntity;
import com.enterprise.ai.infrastructure.persistence.AiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final NlpAnalyticsService textToSqlService;
    private final RecommendationService recommendationService;
    private final AiConversationRepository conversationRepo;
    private final AiMessageRepository messageRepo;

    @PostMapping("/query")
    public ResponseEntity<NlQueryResponse> processQuery(@RequestBody NlQueryRequest request) {
        return ResponseEntity.ok(textToSqlService.translate(request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<AiConversationEntity>> listConversations(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID tenantId = tenantIdHeader != null ? UUID.fromString(tenantIdHeader) : UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.fromString("00000000-0000-0000-0000-000000000002");
        return ResponseEntity.ok(conversationRepo.findByUserIdAndTenantIdOrderByUpdatedAtDesc(userId, tenantId));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<AiMessageEntity>> getMessageHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(messageRepo.findByConversationIdOrderByCreatedAtAsc(id));
    }

    @PostMapping("/recommendations/kpi")
    public ResponseEntity<RecommendationResponse> getKpiRecommendations(@RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(recommendationService.generateRecommendations(request));
    }

    @PostMapping("/recommendations/goal")
    public ResponseEntity<Object> getGoalRecommendations() {
        return ResponseEntity.ok(new Object());
    }
}
