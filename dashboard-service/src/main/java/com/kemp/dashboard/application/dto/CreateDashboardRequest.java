package com.kemp.dashboard.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDashboardRequest {
    @NotBlank
    private String name;
    private String description;
    private Boolean fullscreenEnabled;
    private Boolean autoRotate;
    private Integer autoRotateInterval;
    private String layoutJson;
}
