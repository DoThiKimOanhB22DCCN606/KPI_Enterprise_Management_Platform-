package com.kemp.dashboard.application.dto;

import com.kemp.dashboard.domain.model.WidgetType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class AddWidgetRequest {
    @NotNull
    private WidgetType widgetType;
    private String title;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Map<String, Object> configJson;
}
