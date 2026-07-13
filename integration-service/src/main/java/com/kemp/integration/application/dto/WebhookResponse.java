package com.kemp.integration.application.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookResponse {
    private UUID id;
    private UUID tenantId;
    private String url;
    private List<String> events;
    private Boolean active;
}
