package com.kemp.dashboard.domain.model;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardWidget {
    private UUID id;
    private UUID dashboardId;
    private WidgetType widgetType;
    private String title;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Map<String, Object> configJson;
}
