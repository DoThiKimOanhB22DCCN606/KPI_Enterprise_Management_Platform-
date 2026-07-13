package com.kemp.tenant.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTenantRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String timezone;
}
