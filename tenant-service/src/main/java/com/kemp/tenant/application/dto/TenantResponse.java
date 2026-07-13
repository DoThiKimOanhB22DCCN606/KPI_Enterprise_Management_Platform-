package com.kemp.tenant.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantResponse {
    private UUID id;
    private String code;
    private String name;
    private String logoUrl;
    private String status;
    private String timezone;
    private OffsetDateTime createdAt;
}
