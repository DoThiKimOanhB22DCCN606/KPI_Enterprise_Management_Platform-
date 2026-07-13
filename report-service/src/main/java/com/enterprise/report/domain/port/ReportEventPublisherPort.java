package com.enterprise.report.domain.port;

import java.util.UUID;

public interface ReportEventPublisherPort {
    void publishReportGeneratedEvent(String eventId, UUID tenantId, UUID userId, String downloadUrl);
}
