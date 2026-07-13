package com.kemp.integration.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiTokenResponse {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String name;
    private LocalDateTime expiredAt;
    private String token;
}
