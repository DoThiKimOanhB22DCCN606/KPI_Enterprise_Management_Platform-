package com.kemp.dashboard.application.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class DashboardResponse implements Serializable {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private String publicToken;
    private Boolean fullscreenEnabled;
    private Boolean autoRotate;
    private Integer autoRotateInterval;
    private UUID createdBy;
    private String layoutJson;
    private List<WidgetResponse> widgets;
}
