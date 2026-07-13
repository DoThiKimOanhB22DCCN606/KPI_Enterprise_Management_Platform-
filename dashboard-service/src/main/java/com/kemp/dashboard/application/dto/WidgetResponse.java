package com.kemp.dashboard.application.dto;

import com.kemp.dashboard.domain.model.WidgetType;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class WidgetResponse implements Serializable {
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
