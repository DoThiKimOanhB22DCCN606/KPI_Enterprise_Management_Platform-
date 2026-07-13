package com.enterprise.report.domain.port;

public interface ReportStoragePort {
    /**
     * Saves the report bytes and returns a download URL
     */
    String saveReport(String requestId, byte[] content, String fileExtension);
}
