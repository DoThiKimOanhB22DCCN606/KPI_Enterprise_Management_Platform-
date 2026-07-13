package com.enterprise.goal.infrastructure.messaging;

import com.enterprise.goal.application.service.GoalRecalculationService;
import com.enterprise.goal.infrastructure.messaging.event.KpiProgressUpdatedEvent;
import com.enterprise.goal.infrastructure.persistence.ProcessedEventEntity;
import com.enterprise.goal.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiProgressEventListener {

    private final GoalRecalculationService recalculationService;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queues = "${rabbitmq.queue.kpi.progress:goal-service.kpi.progress.queue}")
    public void handleKpiProgressUpdated(KpiProgressUpdatedEvent event) {
        log.info("Received KpiProgressUpdatedEvent: {}", event.getEventId());

        // Idempotency Check
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Event {} already processed. Skipping.", event.getEventId());
            return;
        }

        try {
            recalculationService.recalculateCascadingGoals(event);

            // Mark as processed
            processedEventRepository.save(new ProcessedEventEntity(event.getEventId(), OffsetDateTime.now()));
            log.info("Successfully processed and recorded event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to process KpiProgressUpdatedEvent: {}", event.getEventId(), e);
            throw e; // Throwing allows RabbitMQ to retry or send to DLQ based on config
        }
    }
}
