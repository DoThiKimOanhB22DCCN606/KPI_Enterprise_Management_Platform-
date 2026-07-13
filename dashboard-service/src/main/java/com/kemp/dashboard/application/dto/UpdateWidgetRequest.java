package com.kemp.dashboard.application.dto;

import java.util.Map;
import lombok.Data;

@Data
public class UpdateWidgetRequest {
    private String title;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Map<String, Object> configJson;
}
