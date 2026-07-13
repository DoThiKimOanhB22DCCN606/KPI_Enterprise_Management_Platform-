package com.enterprise.analytics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataPoint {
    private Date month;
    private Double avgValue;
    private Double maxValue;
    private Double minValue;
    private Long dataPoints;
}
