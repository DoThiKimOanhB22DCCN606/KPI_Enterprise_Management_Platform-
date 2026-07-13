package com.enterprise.report.application.service;

import com.enterprise.report.domain.model.ReportRequest;
import com.enterprise.report.domain.port.ReportEventPublisherPort;
import com.enterprise.report.domain.port.ReportGeneratorPort;
import com.enterprise.report.domain.port.ReportStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReportService {

    private final List<ReportGeneratorPort> generators;
    private final ReportStoragePort storagePort;
    private final ReportEventPublisherPort eventPublisherPort;

    private final Map<String, ReportStatus> statusMap = new ConcurrentHashMap<>();

    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED, NOT_FOUND
    }

    public record ReportStatus(Status status, String downloadUrl, String errorMessage) {}

    public ReportStatus getStatus(String requestId) {
        return statusMap.getOrDefault(requestId, new ReportStatus(Status.NOT_FOUND, null, null));
    }

    public void initReport(String requestId) {
        statusMap.put(requestId, new ReportStatus(Status.PENDING, null, null));
    }

    @Async
    public void processReport(ReportRequest request) {
        statusMap.put(request.getRequestId(), new ReportStatus(Status.PROCESSING, null, null));
        log.info("Started processing async report request: {}", request.getRequestId());

        try {
            // 1. Find correct generator
            ReportGeneratorPort generator = generators.stream()
                    .filter(g -> g.supports(request.getFormat()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No generator found for format: " + request.getFormat()));

            // 2. Generate Bytes
            byte[] reportContent = generator.generateReport(request);

            // 3. Store file and get URL
            String extension = request.getFormat().name().toLowerCase();
            String downloadUrl = storagePort.saveReport(request.getRequestId(), reportContent, extension);

            // 4. Publish Event
            eventPublisherPort.publishReportGeneratedEvent(
                    UUID.randomUUID().toString(),
                    request.getTenantId(),
                    request.getRequestingUserId(),
                    downloadUrl
            );

            statusMap.put(request.getRequestId(), new ReportStatus(Status.COMPLETED, downloadUrl, null));
            log.info("Report generated successfully. Download URL: {}", downloadUrl);

        } catch (Exception e) {
            log.error("Failed to generate report for request: {}", request.getRequestId(), e);
            statusMap.put(request.getRequestId(), new ReportStatus(Status.FAILED, null, e.getMessage()));
            // In a real system, we'd publish a ReportFailedEvent here to notify the user.
        }
    }
}
