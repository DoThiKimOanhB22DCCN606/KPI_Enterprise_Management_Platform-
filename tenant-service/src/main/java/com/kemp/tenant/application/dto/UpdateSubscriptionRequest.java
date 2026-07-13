package com.kemp.tenant.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class UpdateSubscriptionRequest {
    @NotBlank
    private String planType;
    @NotNull
    private Integer maxUsers;
    @NotNull
    private Integer maxKpis;
    private OffsetDateTime expiresAt;
}
