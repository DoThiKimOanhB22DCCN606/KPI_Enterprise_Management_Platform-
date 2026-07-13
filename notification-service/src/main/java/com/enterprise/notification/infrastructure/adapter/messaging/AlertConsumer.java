package com.enterprise.notification.infrastructure.adapter.messaging;

import com.enterprise.notification.application.service.NotificationDispatcherService;
import com.enterprise.notification.domain.model.AlertTriggeredEvent;
import com.enterprise.notification.infrastructure.persistence.ProcessedEventEntity;
import com.enterprise.notification.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertConsumer {

    private final NotificationDispatcherService dispatcherService;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queues = "${rabbitmq.queue.notification.alert:notification-service.alert.queue}")
    public void handleAlertTriggered(AlertTriggeredEvent event) {
        if (isProcessed(event.getEventId())) return;
        
        log.info("Received AlertTriggeredEvent: {} with severity: {}", event.getEventId(), event.getSeverity());
        
        try {
            dispatcherService.dispatch(event);
            markProcessed(event.getEventId());
        } catch (Exception e) {
            log.error("Failed to process AlertTriggeredEvent: {}", event.getEventId(), e);
            throw e; // Allows RabbitMQ to retry or push to DLQ
        }
    }

    private boolean isProcessed(String eventId) {
        if (processedEventRepository.existsById(eventId)) {
            log.info("Event {} already processed. Skipping.", eventId);
            return true;
        }
        return false;
    }

    private void markProcessed(String eventId) {
        processedEventRepository.save(new ProcessedEventEntity(eventId, OffsetDateTime.now()));
    }
}
