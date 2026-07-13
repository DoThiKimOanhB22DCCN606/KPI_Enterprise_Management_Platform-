package com.enterprise.audit.infrastructure.messaging;

import com.enterprise.audit.application.dto.AuditEventDTO;
import com.enterprise.audit.application.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditService auditService;

    @RabbitListener(queues = "${rabbitmq.queue.audit:audit-service.logs.queue}")
    public void handleAuditEvent(AuditEventDTO event) {
        try {
            auditService.recordEvent(event);
        } catch (Exception e) {
            log.error("Failed to record audit event: {}", event.getEventId(), e);
            throw e; // Rely on RabbitMQ retry mechanics
        }
    }
}
