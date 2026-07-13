package com.kemp.integration.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class CreateWebhookRequest {
    @NotBlank
    private String url;
    @NotEmpty
    private List<String> events;
}
