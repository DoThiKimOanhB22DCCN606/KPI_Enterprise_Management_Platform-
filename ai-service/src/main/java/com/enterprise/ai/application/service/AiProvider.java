package com.enterprise.ai.application.service;

import com.enterprise.ai.application.dto.IntentExtractionResult;

import java.util.List;
import java.util.Map;

public interface AiProvider {
    IntentExtractionResult extractIntent(String prompt);
    String generateSummary(String prompt, List<Map<String, Object>> data);
}
