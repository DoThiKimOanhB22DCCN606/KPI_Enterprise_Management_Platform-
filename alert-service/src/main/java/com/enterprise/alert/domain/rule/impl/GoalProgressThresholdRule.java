package com.enterprise.alert.domain.rule.impl;

import com.enterprise.alert.domain.event.AlertTriggeredEvent;
import com.enterprise.alert.domain.event.GoalRecalculatedEvent;
import com.enterprise.alert.domain.event.Severity;
import com.enterprise.alert.domain.rule.AlertRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class GoalProgressThresholdRule implements AlertRule<GoalRecalculatedEvent> {

    @Override
    public boolean supports(Class<?> eventClass) {
        return GoalRecalculatedEvent.class.isAssignableFrom(eventClass);
    }

    @Override
    public Optional<AlertTriggeredEvent> evaluate(GoalRecalculatedEvent event) {
        
        BigDecimal progress = event.getOverallProgress();

        if (progress.compareTo(new BigDecimal("30.0")) < 0) {
            return Optional.of(buildAlert(event, Severity.CRITICAL, "Goal Progress critically low (below 30%)"));
        }

        return Optional.empty(); // No alert needed
    }

    private AlertTriggeredEvent buildAlert(GoalRecalculatedEvent event, Severity severity, String message) {
        return AlertTriggeredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .tenantId(event.getTenantId())
                .sourceId(event.getGoalId().toString())
                .sourceType("GOAL")
                .severity(severity)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
