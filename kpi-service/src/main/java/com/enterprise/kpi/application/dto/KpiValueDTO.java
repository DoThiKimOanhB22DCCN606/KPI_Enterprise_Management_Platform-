package com.enterprise.kpi.application.dto;

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
public class KpiValueDTO {
    private UUID id;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private BigDecimal actualValue;
    private BigDecimal progressPercent;
    private String comment;
    private String evidence;
    private List<AttachmentResponse> attachments;
}
