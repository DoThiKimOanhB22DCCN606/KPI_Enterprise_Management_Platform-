package com.enterprise.report.domain.port;

import com.enterprise.report.domain.model.ReportRequest;

public interface ReportGeneratorPort {
    /**
     * Generates a report in the specified format and returns raw bytes.
     */
    byte[] generateReport(ReportRequest request);
    
    /**
     * Identifies which format this generator supports.
     */
    boolean supports(com.enterprise.report.domain.model.ReportFormat format);
}
