package com.enterprise.notification.infrastructure.adapter.messaging;

import com.enterprise.notification.domain.model.EscalationLevel;
import com.enterprise.notification.domain.model.NotificationChannel;
import com.enterprise.notification.domain.model.NotificationSentEvent;
import com.enterprise.notification.domain.port.NotificationEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQNotificationEventPublisher implements NotificationEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:kemp.events.exchange}")
    private String exchangeName;

    private static final String ROUTING_KEY = "notification.sent";

    @Override
    public void publishNotificationSentEvent(String eventId, UUID tenantId, String sourceEventId, 
                                             NotificationChannel channel, EscalationLevel escalationLevel, boolean success) {
        
        NotificationSentEvent event = NotificationSentEvent.builder()
                .eventId(eventId)
                .tenantId(tenantId)
                .sourceEventId(sourceEventId)
                .channel(channel)
                .escalationLevel(escalationLevel)
                .status(success ? "SUCCESS" : "FAILED")
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Publishing NotificationSentEvent for channel: {} status: {}", channel, event.getStatus());
        rabbitTemplate.convertAndSend(exchangeName, ROUTING_KEY, event);
    }
}
