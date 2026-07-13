package com.enterprise.kpi.application.dto;

import com.enterprise.kpi.domain.model.KpiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDTO {
    private UUID id;
    private UUID tenantId;
    private UUID templateId;
    private UUID ownerId;
    private String name;
    private String frequency;
    private KpiStatus status;
    private BigDecimal target;
    private BigDecimal currentProgress;
    private String formula;
    private BigDecimal evaluationScore;
    private String managerComments;
    private UUID cycleId;
    private OffsetDateTime updatedAt;
    private List<KpiValueDTO> values;
}
