package com.kemp.integration.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookSubscription {
    private UUID id;
    private UUID tenantId;
    private String url;
    private List<String> events;
    private String secretHash;
    private Boolean active;
}
