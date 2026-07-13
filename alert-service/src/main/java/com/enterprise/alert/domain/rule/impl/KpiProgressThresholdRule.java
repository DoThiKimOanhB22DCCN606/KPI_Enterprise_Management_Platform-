package com.enterprise.alert.domain.rule.impl;

import com.enterprise.alert.domain.event.AlertTriggeredEvent;
import com.enterprise.alert.domain.event.KpiDroppedEvent;
import com.enterprise.alert.domain.event.KpiProgressUpdatedEvent;
import com.enterprise.alert.domain.event.Severity;
import com.enterprise.alert.domain.rule.AlertRule;
import com.enterprise.alert.infrastructure.messaging.AlertPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KpiProgressThresholdRule implements AlertRule<KpiProgressUpdatedEvent> {

    private final AlertPublisher alertPublisher;

    @Override
    public boolean supports(Class<?> eventClass) {
        return KpiProgressUpdatedEvent.class.isAssignableFrom(eventClass);
    }

    @Override
    public Optional<AlertTriggeredEvent> evaluate(KpiProgressUpdatedEvent event) {
        
        BigDecimal progress = event.getNewProgress();

        if (progress.compareTo(new BigDecimal("40.0")) < 0) {
            // Trigger specific KPI dropped event to the new exchange
            KpiDroppedEvent droppedEvent = KpiDroppedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .tenantId(event.getTenantId())
                    .kpiId(event.getKpiId())
                    .currentProgress(progress)
                    .severity(Severity.CRITICAL)
                    .message("KPI Progress critically low (below 40%)")
                    .timestamp(System.currentTimeMillis())
                    .build();
            alertPublisher.publishKpiDropped(droppedEvent);

            return Optional.of(buildAlert(event, Severity.CRITICAL, "KPI Progress critically low (below 40%)"));
        } else if (progress.compareTo(new BigDecimal("60.0")) < 0) {
            return Optional.of(buildAlert(event, Severity.WARNING, "KPI Progress warning (below 60%)"));
        }

        return Optional.empty();
    }

    private AlertTriggeredEvent buildAlert(KpiProgressUpdatedEvent event, Severity severity, String message) {
        return AlertTriggeredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .tenantId(event.getTenantId())
                .sourceId(event.getKpiId().toString())
                .sourceType("KPI")
                .severity(severity)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
