package com.kemp.dashboard.application.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class UpdateLayoutRequest {
    @NotEmpty
    private List<WidgetLayoutDto> layout;
}
