package com.enterprise.report.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private String requestId;
    private UUID tenantId;
    private UUID requestingUserId;
    private ReportFormat format;
    private String title;
    
    // In a real scenario, this might contain query parameters, filters, or raw data to report on
    // For MVP, we'll assume it generates a standard summary report
}
