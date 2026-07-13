package com.enterprise.alert.infrastructure.messaging;

import com.enterprise.alert.application.service.RuleEngineService;
import com.enterprise.alert.domain.event.AlertTriggeredEvent;
import com.enterprise.alert.domain.event.GoalRecalculatedEvent;
import com.enterprise.alert.domain.event.KpiProgressUpdatedEvent;
import com.enterprise.alert.infrastructure.persistence.ProcessedEventEntity;
import com.enterprise.alert.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final RuleEngineService ruleEngineService;
    private final AlertPublisher alertPublisher;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queues = "${rabbitmq.queue.alert.kpi:alert-service.kpi.progress.queue}")
    public void handleKpiProgressUpdated(KpiProgressUpdatedEvent event) {
        if (isProcessed(event.getEventId())) return;
        
        log.info("Evaluating KPI event: {}", event.getEventId());
        evaluateAndPublish(event);
        markProcessed(event.getEventId());
    }

    @RabbitListener(queues = "${rabbitmq.queue.alert.goal:alert-service.goal.recalculated.queue}")
    public void handleGoalRecalculated(GoalRecalculatedEvent event) {
        if (isProcessed(event.getEventId())) return;
        
        log.info("Evaluating Goal event: {}", event.getEventId());
        evaluateAndPublish(event);
        markProcessed(event.getEventId());
    }

    private void evaluateAndPublish(Object event) {
        List<AlertTriggeredEvent> alerts = ruleEngineService.evaluateRules(event);
        for (AlertTriggeredEvent alert : alerts) {
            alertPublisher.publishAlert(alert);
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
