package com.enterprise.ai.application.service;

import com.enterprise.ai.application.dto.IntentExtractionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.demo-mode", havingValue = "false", matchIfMissing = true)
public class ChatClientAiProvider implements AiProvider {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are an intent extraction assistant for the KEMP analytics platform.
            Your task is to analyze the user's natural language question and extract the intent and parameters.
            For TREND_COMPARISON intent, you MUST also extract: entity1, entity2, kpiKeyword, and months (default 3).
            Do not try to answer the question, only extract the structured data.
            """;

    public ChatClientAiProvider(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Override
    public IntentExtractionResult extractIntent(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(IntentExtractionResult.class);
    }

    @Override
    public String generateSummary(String prompt, List<Map<String, Object>> data) {
        return chatClient.prompt()
                .user("Format these DB results into a short, human-readable sentence answering: " + prompt + " \\nResults: " + data)
                .call()
                .content();
    }
}
