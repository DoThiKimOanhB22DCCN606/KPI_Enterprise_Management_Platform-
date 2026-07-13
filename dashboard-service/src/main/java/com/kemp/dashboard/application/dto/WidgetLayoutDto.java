package com.kemp.dashboard.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class WidgetLayoutDto {
    @NotNull
    private UUID widgetId;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
}
