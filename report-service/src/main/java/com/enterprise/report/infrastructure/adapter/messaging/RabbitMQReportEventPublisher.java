package com.enterprise.report.infrastructure.adapter.messaging;

import com.enterprise.report.domain.port.ReportEventPublisherPort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMQReportEventPublisher implements ReportEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:kemp.events.exchange}")
    private String exchangeName;

    private static final String ROUTING_KEY = "report.generated";

    @Override
    public void publishReportGeneratedEvent(String eventId, UUID tenantId, UUID userId, String downloadUrl) {
        ReportGeneratedEvent event = ReportGeneratedEvent.builder()
                .eventId(eventId)
                .tenantId(tenantId)
                .userId(userId)
                .downloadUrl(downloadUrl)
                .timestamp(System.currentTimeMillis())
                .build();

        rabbitTemplate.convertAndSend(exchangeName, ROUTING_KEY, event);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportGeneratedEvent {
        private String eventId;
        private UUID tenantId;
        private UUID userId;
        private String downloadUrl;
        private long timestamp;
    }
}
