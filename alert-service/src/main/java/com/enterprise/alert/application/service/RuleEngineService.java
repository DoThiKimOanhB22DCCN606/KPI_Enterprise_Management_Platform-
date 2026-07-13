package com.enterprise.alert.application.service;

import com.enterprise.alert.domain.event.AlertTriggeredEvent;
import com.enterprise.alert.domain.rule.AlertRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngineService {

    // Spring will automatically inject all beans implementing AlertRule
    private final List<AlertRule> rules;

    /**
     * Evaluates the given event against all registered rules.
     * Returns a list of generated alerts.
     */
    @SuppressWarnings("unchecked")
    public List<AlertTriggeredEvent> evaluateRules(Object event) {
        List<AlertTriggeredEvent> generatedAlerts = new ArrayList<>();

        for (AlertRule rule : rules) {
            if (rule.supports(event.getClass())) {
                try {
                    Optional<AlertTriggeredEvent> alertOpt = rule.evaluate(event);
                    alertOpt.ifPresent(alert -> {
                        log.info("Rule {} triggered an alert: {}", rule.getClass().getSimpleName(), alert.getMessage());
                        generatedAlerts.add(alert);
                    });
                } catch (Exception e) {
                    log.error("Error evaluating rule: {}", rule.getClass().getSimpleName(), e);
                }
            }
        }

        return generatedAlerts;
    }
}
