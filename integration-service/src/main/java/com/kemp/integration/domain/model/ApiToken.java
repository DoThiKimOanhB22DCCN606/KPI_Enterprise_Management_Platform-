package com.kemp.integration.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiToken {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String tokenHash;
    private String name;
    private LocalDateTime expiredAt;
}
