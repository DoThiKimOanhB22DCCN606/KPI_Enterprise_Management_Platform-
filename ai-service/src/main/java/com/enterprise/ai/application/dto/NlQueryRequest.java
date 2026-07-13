package com.enterprise.ai.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class NlQueryRequest {
    private String prompt;
    private String context;
    private UUID conversationId;
}
