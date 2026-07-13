package com.kemp.integration.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateApiTokenRequest {
    @NotBlank
    private String name;
}
