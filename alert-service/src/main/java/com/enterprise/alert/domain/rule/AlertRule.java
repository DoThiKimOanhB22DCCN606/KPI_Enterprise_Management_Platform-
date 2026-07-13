package com.enterprise.alert.domain.rule;

import com.enterprise.alert.domain.event.AlertTriggeredEvent;

import java.util.Optional;

/**
 * Strategy interface for evaluating an event against a business rule.
 * The generic type T can be any event payload.
 */
public interface AlertRule<T> {
    
    /**
     * Checks if this rule knows how to process the given event type.
     */
    boolean supports(Class<?> eventClass);

    /**
     * Evaluates the event and returns an AlertTriggeredEvent if the threshold is breached.
     */
    Optional<AlertTriggeredEvent> evaluate(T event);
}
