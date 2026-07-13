package com.kemp.tenant.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantSubscription {
    private UUID id;
    private UUID tenantId;
    private String planType;
    private Integer maxUsers;
    private Integer maxKpis;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
