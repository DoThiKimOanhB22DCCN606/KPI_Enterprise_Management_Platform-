package com.enterprise.notification.infrastructure.adapter.messaging;

import com.enterprise.notification.application.service.NotificationDispatcherService;
import com.enterprise.notification.domain.exception.NotificationDeliveryException;
import com.enterprise.notification.domain.model.KpiDroppedEvent;
import com.enterprise.notification.infrastructure.persistence.ProcessedEventEntity;
import com.enterprise.notification.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiDroppedConsumer {

    private final NotificationDispatcherService dispatcherService;
    private final ProcessedEventRepository processedEventRepository;

    @Retryable(
        retryFor = { NotificationDeliveryException.class, Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @RabbitListener(queues = "${rabbitmq.queue.notification.kpi-dropped:notification-service.alert.kpi-dropped}")
    public void handleKpiDropped(KpiDroppedEvent event) {
        if (isProcessed(event.getEventId())) {
            return;
        }

        log.info("Received KpiDroppedEvent: {} with severity: {}", event.getEventId(), event.getSeverity());

        // Dispatch logic handles sending and will throw NotificationDeliveryException if it fails
        dispatcherService.dispatch(event);

        markProcessed(event.getEventId());
    }

    @Recover
    public void recover(Exception e, KpiDroppedEvent event) {
        log.error("All retries exhausted for KpiDroppedEvent {}. Sending to DLQ.", event.getEventId(), e);
        // By throwing a runtime exception here, Spring AMQP will reject the message
        // and because of the DLX configuration on the queue, it will be routed to the DLQ.
        throw new RuntimeException("Failed to process event after retries", e);
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
