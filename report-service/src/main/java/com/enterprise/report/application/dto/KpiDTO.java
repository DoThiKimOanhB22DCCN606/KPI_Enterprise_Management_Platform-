package com.enterprise.report.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class KpiDTO {
    private UUID id;
    private String name;
    private UUID ownerId;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String status;
    private String updateFrequency;
}
