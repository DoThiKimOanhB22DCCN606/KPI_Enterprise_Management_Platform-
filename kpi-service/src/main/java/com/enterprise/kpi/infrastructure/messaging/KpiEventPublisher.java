package com.enterprise.kpi.infrastructure.messaging;

import com.enterprise.kpi.infrastructure.messaging.event.KpiProgressUpdatedEvent;
import com.enterprise.kpi.infrastructure.messaging.event.AuditEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KpiEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:kemp.events.exchange}")
    private String exchangeName;

    private static final String ROUTING_KEY = "kpi.progress.updated";

    public void publishProgressUpdatedEvent(UUID tenantId, UUID kpiId, BigDecimal oldProgress, BigDecimal newProgress) {
        KpiProgressUpdatedEvent event = KpiProgressUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .kpiId(kpiId)
                .oldProgress(oldProgress)
                .newProgress(newProgress)
                .timestamp(System.currentTimeMillis())
                .build();

        rabbitTemplate.convertAndSend(exchangeName, ROUTING_KEY, event);
    }

    public void publishAuditEvent(AuditEventDTO event) {
        rabbitTemplate.convertAndSend(exchangeName, "audit.event.recorded", event);
    }
}
