package com.kemp.dashboard.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Dashboard {
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
    private List<DashboardWidget> widgets;
}
