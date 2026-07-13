package com.enterprise.report.application.dto;

import com.enterprise.report.domain.model.ReportFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GenerateReportRequestDTO {
    
    @NotNull(message = "tenantId is required")
    private UUID tenantId;
    
    @NotNull(message = "requestingUserId is required")
    private UUID requestingUserId;
    
    @NotNull(message = "format is required")
    private ReportFormat format;
    
    private String title;
}
